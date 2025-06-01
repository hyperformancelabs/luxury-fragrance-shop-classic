-- Triggers for automatic inventory management
-- This migration creates SQL Server triggers to automatically:
-- 1. Decrease product variant stock when order items are inserted/updated
-- 2. Increase product variant stock when orders are canceled or order items are deleted
-- 3. Log all inventory changes to the InventoryTransaction table

-- Trigger to automatically decrease inventory when order items are inserted
CREATE OR ALTER TRIGGER TR_OrderItem_Insert
ON [OrderItem]
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Create a table variable to store inventory changes
    DECLARE @InventoryChanges TABLE (
        product_variant_id INT,
        quantity INT,
        before_quantity INT,
        after_quantity INT
    );
    
    -- Calculate inventory changes
    INSERT INTO @InventoryChanges (product_variant_id, quantity, before_quantity, after_quantity)
    SELECT 
        i.product_variant_id,
        i.quantity,
        pv.quantity_in_stock,
        pv.quantity_in_stock - i.quantity
    FROM 
        inserted i
        INNER JOIN [ProductVariant] pv ON i.product_variant_id = pv.product_variant_id
        INNER JOIN [Order] o ON i.order_id = o.order_id
    WHERE 
        o.order_status NOT IN ('cancelled'); -- Only decrease inventory for non-cancelled orders
    
    -- Update product variant inventory
    UPDATE pv
    SET pv.quantity_in_stock = CASE
                                  WHEN pv.quantity_in_stock - ic.quantity < 0 THEN 0
                                  ELSE pv.quantity_in_stock - ic.quantity
                               END
    FROM 
        [ProductVariant] pv
        INNER JOIN @InventoryChanges ic ON pv.product_variant_id = ic.product_variant_id;
    
    -- Log inventory transactions
    INSERT INTO [InventoryTransaction] (
        product_variant_id,
        performed_by,
        transaction_type,
        transaction_date,
        before_quantity,
        quantity,
        after_quantity,
        reason,
        note
    )
    SELECT
        ic.product_variant_id,
        o.employee_id,
        'sell', -- Transaction type for sales
        GETDATE(),
        ic.before_quantity,
        ic.quantity,
        ic.after_quantity,
        'Order Placed',
        'Order #' + CAST(o.order_id AS NVARCHAR(20)) + ' - Automatic inventory adjustment'
    FROM
        @InventoryChanges ic
        INNER JOIN inserted i ON ic.product_variant_id = i.product_variant_id
        INNER JOIN [Order] o ON i.order_id = o.order_id;
END;
GO

-- Trigger to automatically increase inventory when order items are deleted
CREATE OR ALTER TRIGGER TR_OrderItem_Delete
ON [OrderItem]
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Create a table variable to store inventory changes
    DECLARE @InventoryChanges TABLE (
        product_variant_id INT,
        quantity INT,
        before_quantity INT,
        after_quantity INT,
        order_id INT
    );
    
    -- Calculate inventory changes
    INSERT INTO @InventoryChanges (product_variant_id, quantity, before_quantity, after_quantity, order_id)
    SELECT 
        d.product_variant_id,
        d.quantity,
        pv.quantity_in_stock,
        pv.quantity_in_stock + d.quantity,
        d.order_id
    FROM 
        deleted d
        INNER JOIN [ProductVariant] pv ON d.product_variant_id = pv.product_variant_id
        INNER JOIN [Order] o ON d.order_id = o.order_id
    WHERE 
        o.order_status NOT IN ('cancelled', 'delivered'); -- Only restore inventory for non-cancelled and non-delivered orders
    
    -- Update product variant inventory
    UPDATE pv
    SET pv.quantity_in_stock = pv.quantity_in_stock + ic.quantity
    FROM 
        [ProductVariant] pv
        INNER JOIN @InventoryChanges ic ON pv.product_variant_id = ic.product_variant_id;
    
    -- Log inventory transactions
    INSERT INTO [InventoryTransaction] (
        product_variant_id,
        performed_by,
        transaction_type,
        transaction_date,
        before_quantity,
        quantity,
        after_quantity,
        reason,
        note
    )
    SELECT
        ic.product_variant_id,
        o.employee_id,
        'import', -- Transaction type for returns
        GETDATE(),
        ic.before_quantity,
        ic.quantity,
        ic.after_quantity,
        'Order Item Deleted',
        'Order #' + CAST(o.order_id AS NVARCHAR(20)) + ' - Automatic inventory adjustment'
    FROM
        @InventoryChanges ic
        INNER JOIN [Order] o ON ic.order_id = o.order_id;
END;
GO

-- Trigger to handle order cancellation and restore inventory
CREATE OR ALTER TRIGGER TR_Order_StatusUpdate
ON [Order]
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if order status was changed to cancelled
    IF UPDATE(order_status) AND EXISTS (
        SELECT 1 FROM inserted i
        INNER JOIN deleted d ON i.order_id = d.order_id
        WHERE i.order_status = 'cancelled' AND d.order_status <> 'cancelled'
    )
    BEGIN
        -- Create a table variable to store inventory changes
        DECLARE @InventoryChanges TABLE (
            product_variant_id INT,
            quantity INT,
            before_quantity INT,
            after_quantity INT,
            order_item_id INT
        );
        
        -- Calculate inventory changes for cancelled orders
        INSERT INTO @InventoryChanges (product_variant_id, quantity, before_quantity, after_quantity, order_item_id)
        SELECT 
            oi.product_variant_id,
            oi.quantity,
            pv.quantity_in_stock,
            pv.quantity_in_stock + oi.quantity,
            oi.order_item_id
        FROM 
            [OrderItem] oi
            INNER JOIN inserted i ON oi.order_id = i.order_id
            INNER JOIN [ProductVariant] pv ON oi.product_variant_id = pv.product_variant_id
        WHERE 
            i.order_status = 'cancelled';
        
        -- Update product variant inventory
        UPDATE pv
        SET pv.quantity_in_stock = pv.quantity_in_stock + ic.quantity
        FROM 
            [ProductVariant] pv
            INNER JOIN @InventoryChanges ic ON pv.product_variant_id = ic.product_variant_id;
        
        -- Log inventory transactions
        INSERT INTO [InventoryTransaction] (
            product_variant_id,
            performed_by,
            transaction_type,
            transaction_date,
            before_quantity,
            quantity,
            after_quantity,
            reason,
            note
        )
        SELECT
            ic.product_variant_id,
            i.employee_id,
            'import', -- Transaction type for returns from cancellation
            GETDATE(),
            ic.before_quantity,
            ic.quantity,
            ic.after_quantity,
            'Order Cancelled',
            'Order #' + CAST(i.order_id AS NVARCHAR(20)) + ' - Automatic inventory adjustment'
        FROM
            @InventoryChanges ic
            INNER JOIN inserted i ON i.order_status = 'cancelled';
    END;
END;
GO

-- Trigger to handle updates to order items (quantity changes)
CREATE OR ALTER TRIGGER TR_OrderItem_Update
ON [OrderItem]
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if quantity was updated
    IF UPDATE(quantity)
    BEGIN
        -- Create a table variable to store inventory changes
        DECLARE @InventoryChanges TABLE (
            product_variant_id INT,
            old_quantity INT,
            new_quantity INT,
            quantity_diff INT,
            before_quantity INT,
            after_quantity INT,
            order_id INT
        );
        
        -- Calculate inventory changes based on quantity difference
        INSERT INTO @InventoryChanges (
            product_variant_id, 
            old_quantity, 
            new_quantity, 
            quantity_diff, 
            before_quantity, 
            after_quantity,
            order_id
        )
        SELECT 
            i.product_variant_id,
            d.quantity,
            i.quantity,
            d.quantity - i.quantity, -- Positive means decrease in order quantity (add back to inventory)
            pv.quantity_in_stock,
            pv.quantity_in_stock + (d.quantity - i.quantity),
            i.order_id
        FROM 
            inserted i
            INNER JOIN deleted d ON i.order_item_id = d.order_item_id
            INNER JOIN [ProductVariant] pv ON i.product_variant_id = pv.product_variant_id
            INNER JOIN [Order] o ON i.order_id = o.order_id
        WHERE 
            i.quantity <> d.quantity
            AND o.order_status NOT IN ('cancelled', 'delivered');
        
        -- Update product variant inventory
        UPDATE pv
        SET pv.quantity_in_stock = CASE
                                      WHEN pv.quantity_in_stock + ic.quantity_diff < 0 THEN 0
                                      ELSE pv.quantity_in_stock + ic.quantity_diff
                                   END
        FROM 
            [ProductVariant] pv
            INNER JOIN @InventoryChanges ic ON pv.product_variant_id = ic.product_variant_id;
        
        -- Log inventory transactions for quantity increases (returns to inventory)
        INSERT INTO [InventoryTransaction] (
            product_variant_id,
            performed_by,
            transaction_type,
            transaction_date,
            before_quantity,
            quantity,
            after_quantity,
            reason,
            note
        )
        SELECT
            ic.product_variant_id,
            o.employee_id,
            CASE 
                WHEN ic.quantity_diff > 0 THEN 'import' -- When order quantity decreased
                ELSE 'export' -- When order quantity increased
            END,
            GETDATE(),
            ic.before_quantity,
            ABS(ic.quantity_diff), -- Use absolute value for quantity
            ic.after_quantity,
            'Order Item Updated',
            'Order #' + CAST(o.order_id AS NVARCHAR(20)) + ' - Automatic inventory adjustment'
        FROM
            @InventoryChanges ic
            INNER JOIN [Order] o ON ic.order_id = o.order_id
        WHERE
            ic.quantity_diff <> 0; -- Only log when there's an actual change
    END;
END;
GO

-- Trigger to update ProductVariant inventory when manually changing InventoryTransaction
CREATE OR ALTER TRIGGER TR_InventoryTransaction_Insert
ON [InventoryTransaction]
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Only process manual transactions (not from other triggers)
    -- This check helps prevent trigger recursion
    IF NOT EXISTS (
        SELECT 1 FROM inserted i 
        WHERE i.reason LIKE 'Order%' OR i.reason LIKE '%Automatic%'
    )
    BEGIN
        -- Update ProductVariant based on transaction type
        UPDATE pv
        SET pv.quantity_in_stock = 
            CASE 
                WHEN i.transaction_type = 'import' THEN pv.quantity_in_stock + i.quantity
                WHEN i.transaction_type = 'export' THEN 
                    CASE 
                        WHEN pv.quantity_in_stock - i.quantity < 0 THEN 0
                        ELSE pv.quantity_in_stock - i.quantity
                    END
                WHEN i.transaction_type = 'adjust' THEN i.after_quantity
                ELSE pv.quantity_in_stock
            END
        FROM 
            [ProductVariant] pv
            INNER JOIN inserted i ON pv.product_variant_id = i.product_variant_id
        WHERE 
            -- Skip if before_quantity and after_quantity are both set correctly (likely from another trigger)
            NOT (i.before_quantity IS NOT NULL AND i.after_quantity IS NOT NULL);
            
        -- Update before_quantity and after_quantity if they weren't provided
        UPDATE it
        SET 
            it.before_quantity = CASE 
                                    WHEN it.before_quantity IS NULL THEN pv.quantity_in_stock
                                    ELSE it.before_quantity
                                 END,
            it.after_quantity = CASE
                                  WHEN it.after_quantity IS NULL THEN
                                      CASE 
                                          WHEN it.transaction_type = 'import' THEN pv.quantity_in_stock + it.quantity
                                          WHEN it.transaction_type = 'export' THEN 
                                              CASE 
                                                  WHEN pv.quantity_in_stock - it.quantity < 0 THEN 0
                                                  ELSE pv.quantity_in_stock - it.quantity
                                              END
                                          WHEN it.transaction_type = 'adjust' THEN it.quantity
                                          ELSE pv.quantity_in_stock
                                      END
                                  ELSE it.after_quantity
                               END
        FROM 
            [InventoryTransaction] it
            INNER JOIN inserted i ON it.inventory_transaction_id = i.inventory_transaction_id
            INNER JOIN [ProductVariant] pv ON it.product_variant_id = pv.product_variant_id
        WHERE 
            i.before_quantity IS NULL OR i.after_quantity IS NULL;
    END;
END;
GO 