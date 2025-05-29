-- 1. Table Employee
CREATE TABLE [Employee] (
    [employee_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [username] VARCHAR(50) NOT NULL UNIQUE,
    [password] VARCHAR(255) NOT NULL,
    [full_name] NVARCHAR(100) NOT NULL,
    [phone_number] VARCHAR(20) NOT NULL,
    [email] VARCHAR(100) NULL UNIQUE,
    [address] NVARCHAR(255) NOT NULL,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Employee_Status] DEFAULT 'active'
        CONSTRAINT [CK_Employee_Status] CHECK ([status] IN ('active','inactive','on_leave')),
    [start_date] DATE NOT NULL DEFAULT (GETDATE()),
    [last_login] DATETIME NULL DEFAULT (NULL),
    [date_of_birth] DATE NULL,
    [profile_picture_url] VARCHAR(255) NULL
);
GO

-- 2. Table Role
CREATE TABLE [Role] (
    [role_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [role_name] NVARCHAR(50) NOT NULL UNIQUE,
    [role_description] NVARCHAR(MAX) NULL,
    [is_default] BIT NOT NULL DEFAULT 0,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Role_Status] DEFAULT 'active'
        CONSTRAINT [CK_Role_Status] CHECK ([status] IN ('active','inactive'))
);
GO

-- 3. Table EmployeeRole
CREATE TABLE [EmployeeRole] (
    [employee_role_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [employee_id] INT NOT NULL,
    [role_id] INT NOT NULL,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_EmployeeRole_Status] DEFAULT 'active'
        CONSTRAINT [CK_EmployeeRole_Status] CHECK ([status] IN ('active','inactive')),
    CONSTRAINT [FK_EmployeeRole_Employee] FOREIGN KEY ([employee_id]) REFERENCES [Employee]([employee_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_EmployeeRole_Role] FOREIGN KEY ([role_id]) REFERENCES [Role]([role_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_EmployeeRole] UNIQUE ([employee_id], [role_id])
);
GO

-- 4. Table Permission
CREATE TABLE [Permission] (
    [permission_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [permission_name] NVARCHAR(50) NOT NULL UNIQUE,
    [permission_description] NVARCHAR(MAX) NULL
);
GO

-- 5. Table RolePermission
CREATE TABLE [RolePermission] (
    [role_permission_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [role_id] INT NOT NULL,
    [permission_id] INT NOT NULL,
    CONSTRAINT [FK_RolePermission_Role] FOREIGN KEY ([role_id]) REFERENCES [Role]([role_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_RolePermission_Permission] FOREIGN KEY ([permission_id]) REFERENCES [Permission]([permission_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_RolePermission] UNIQUE ([role_id], [permission_id])
);
GO

-- 6. Table Material
CREATE TABLE [Material] (
    [material_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [material_name] VARCHAR(100) NOT NULL UNIQUE,
    [description] NVARCHAR(MAX) NULL,
    [unit] VARCHAR(20) NOT NULL,
    [quantity_in_stock] INT NOT NULL DEFAULT 0,
    [reorder_level] INT NULL,
    [price] DECIMAL(10,2) NOT NULL
);
GO

-- 7. Table MaterialTransaction
CREATE TABLE [MaterialTransaction] (
    [material_transaction_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [material_id] INT NOT NULL,
    [performed_by] INT NOT NULL,
    [transaction_date] DATETIME NOT NULL,
    [before_quantity] INT NULL,
    [quantity] INT NOT NULL,
    [after_quantity] INT NULL,
    [transaction_type] VARCHAR(20) NOT NULL
        CONSTRAINT [CK_MaterialTransaction_Type] CHECK ([transaction_type] IN ('import','export','adjust')),
    [reason] NVARCHAR(MAX) NULL,
    [note] NVARCHAR(MAX) NULL,
    [cost_price] DECIMAL(10,2) NULL,
    CONSTRAINT [FK_MaterialTransaction_Material] FOREIGN KEY ([material_id]) REFERENCES [Material]([material_id])
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_MaterialTransaction_Employee] FOREIGN KEY ([performed_by]) REFERENCES [Employee]([employee_id])
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
);
GO

-- 8. Table Brand
CREATE TABLE [Brand] (
    [brand_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [brand_name] NVARCHAR(100) NOT NULL UNIQUE,
    [brand_description] NVARCHAR(MAX) NULL,
    [country_of_origin] NVARCHAR(100) NULL,
    [logo_url] VARCHAR(255) NULL,
    [website_url] VARCHAR(255) NULL
);
GO

-- 9. Table Product
CREATE TABLE [Product] (
    [product_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [brand_id] INT NOT NULL,
    [product_name] NVARCHAR(100) NOT NULL,
    [description] NVARCHAR(MAX) NULL,
    [image_url] VARCHAR(500) NULL,
    CONSTRAINT [FK_Product_Brand] FOREIGN KEY ([brand_id]) REFERENCES [Brand]([brand_id])
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_Product_Name_Brand] UNIQUE ([product_name], [brand_id])
);
GO

-- 10. Table ProductVariant
CREATE TABLE [ProductVariant] (
    [product_variant_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [product_id] INT NOT NULL,
    [volume] INT NOT NULL,
    [price] DECIMAL(10,2) NOT NULL,
    [discount_price] DECIMAL(10,2) NULL,
    [quantity_in_stock] INT NOT NULL DEFAULT 0,
    [reorder_level] INT NULL,
    CONSTRAINT [FK_ProductVariant_Product] FOREIGN KEY ([product_id]) REFERENCES [Product]([product_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_ProductVariant] UNIQUE ([product_id], [volume])
);
GO

-- 11. Table InventoryTransaction
CREATE TABLE [InventoryTransaction] (
    [inventory_transaction_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [product_variant_id] INT NOT NULL,
    [performed_by] INT NOT NULL,
    [transaction_type] VARCHAR(20) NOT NULL
        CONSTRAINT [CK_InventoryTransaction_Type] CHECK ([transaction_type] IN ('import','export', 'adjust', 'sell','combine')),
    [transaction_date] DATETIME NOT NULL,
    [before_quantity] INT NULL,
    [quantity] INT NOT NULL,
    [after_quantity] INT NULL,
    [reason] NVARCHAR(MAX) NULL,
    [note] NVARCHAR(MAX) NULL,
    [cost_price] DECIMAL(10,2) NULL,
    CONSTRAINT [FK_InventoryTransaction_ProductVariant] FOREIGN KEY ([product_variant_id]) REFERENCES [ProductVariant]([product_variant_id])
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_InventoryTransaction_Employee] FOREIGN KEY ([performed_by]) REFERENCES [Employee]([employee_id])
        ON DELETE NO ACTION
        ON UPDATE NO ACTION
);
GO

-- 12. Table ProductDetail
CREATE TABLE [ProductDetail] (
    [product_detail_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [product_id] INT NOT NULL,
    [detail_name] VARCHAR(50) NOT NULL
        CONSTRAINT [CK_ProductDetail_Name] CHECK ([detail_name] IN ('tone_scent','style','top_note','middle_note','base_note','longevity','projection','season','time_of_day','suitable_age','suitable_gender')),
    [detail_value] NVARCHAR(255) NOT NULL,
    [note] NVARCHAR(MAX) NULL,
    CONSTRAINT [FK_ProductDetail_Product] FOREIGN KEY ([product_id]) REFERENCES [Product]([product_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_ProductDetail] UNIQUE ([product_id], [detail_name], [detail_value])
);
GO

-- 13. Table Promotion
CREATE TABLE [Promotion] (
    [promotion_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [promotion_name] NVARCHAR(100) NOT NULL,
    [description] NVARCHAR(MAX) NULL,
    [start_date] DATE NOT NULL,
    [end_date] DATE NULL,
    [discount_type] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Promotion_DiscountType] DEFAULT 'percentage'
        CONSTRAINT [CK_Promotion_DiscountType] CHECK ([discount_type] IN ('percentage','fixed_amount','free_shipping')),
    [discount_value] DECIMAL(10,2) NULL,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Promotion_Status] DEFAULT 'active'
        CONSTRAINT [CK_Promotion_Status] CHECK ([status] IN ('active','inactive','expired')),
    [usage_limit] INT NULL
);
GO

-- 14. Table ProductPromotion
CREATE TABLE [ProductPromotion] (
    [product_promotion_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [product_id] INT NOT NULL,
    [promotion_id] INT NOT NULL,
    [condition_json] NVARCHAR(MAX) NULL
        CONSTRAINT [CK_ProductPromotion_JSON] CHECK ([condition_json] IS NULL OR ISJSON([condition_json]) = 1),
    [max_discount_amount] DECIMAL(10,2) NULL,
    [start_date] DATE NOT NULL,
    [end_date] DATE NULL,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_ProductPromotion_Status] DEFAULT 'active'
        CONSTRAINT [CK_ProductPromotion_Status] CHECK ([status] IN ('active','inactive','expired')),
    CONSTRAINT [FK_ProductPromotion_Product] FOREIGN KEY ([product_id]) REFERENCES [Product]([product_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_ProductPromotion_Promotion] FOREIGN KEY ([promotion_id]) REFERENCES [Promotion]([promotion_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_ProductPromotion] UNIQUE ([product_id], [promotion_id])
);
GO

-- 15. Table Customer
CREATE TABLE [Customer] (
    [customer_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [username] VARCHAR(50) NULL,
    [password] VARCHAR(255) NULL,
    [name] NVARCHAR(100) NOT NULL,
    [phone_number] VARCHAR(20) NULL UNIQUE,
    [email] VARCHAR(100) NULL UNIQUE,
    [street] NVARCHAR(255) NULL,
    [ward] NVARCHAR(50) NULL,
    [district] NVARCHAR(50) NULL,
    [city] NVARCHAR(50) NULL,
    [shipping_note] NVARCHAR(50) NULL,
    [note] NVARCHAR(MAX) NULL,
    [rating] INT NOT NULL DEFAULT 10,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Customer_Status] DEFAULT 'active'
        CONSTRAINT [CK_Customer_Status] CHECK ([status] IN ('active','inactive','banned')),
    [loyalty_points] INT NOT NULL DEFAULT 0,
    [create_at] DATETIME NOT NULL DEFAULT (GETDATE()),
    [update_at] DATETIME NULL DEFAULT (NULL)
);
GO

-- 16. Table Cart
CREATE TABLE [Cart] (
    [cart_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [customer_id] INT NULL,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Cart_Status] DEFAULT 'active'
        CONSTRAINT [CK_Cart_Status] CHECK ([status] IN ('active','abandoned','converted')),
    [total_amount] DECIMAL(10,2) NULL,
    [session_id] VARCHAR(50) NULL,
    CONSTRAINT [FK_Cart_Customer] FOREIGN KEY ([customer_id]) REFERENCES [Customer]([customer_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [CK_Cart_CustomerSession] CHECK ([customer_id] IS NOT NULL OR [session_id] IS NOT NULL)
);
GO

-- 17. Table CartItem
CREATE TABLE [CartItem] (
    [cart_item_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [cart_id] INT NOT NULL,
    [product_variant_id] INT NOT NULL,
    [quantity] INT NOT NULL,
    [unit_price] DECIMAL(10,2) NOT NULL,
    [note] NVARCHAR(255) NULL,
    [is_selected] BIT NOT NULL DEFAULT 1,
    CONSTRAINT [FK_CartItem_Cart] FOREIGN KEY ([cart_id]) REFERENCES [Cart]([cart_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_CartItem_ProductVariant] FOREIGN KEY ([product_variant_id]) REFERENCES [ProductVariant]([product_variant_id])
        ON DELETE NO ACTION
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_CartItem] UNIQUE ([cart_id], [product_variant_id])
);
GO

-- 18. Table Wishlist
CREATE TABLE [Wishlist] (
    [wishlist_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [customer_id] INT NOT NULL,
    [product_variant_id] INT NOT NULL,
    [added_date] DATETIME NOT NULL DEFAULT (GETDATE()), -- Ngày thêm vào wishlist

    CONSTRAINT [FK_Wishlist_Customer] FOREIGN KEY ([customer_id]) REFERENCES [Customer]([customer_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [FK_Wishlist_ProductVariant] FOREIGN KEY ([product_variant_id]) REFERENCES [ProductVariant]([product_variant_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [UQ_Wishlist_Customer_ProductVariant] UNIQUE ([customer_id], [product_variant_id])
);
GO


-- 19. Table Order
CREATE TABLE [Order] (
    [order_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [customer_id] INT NOT NULL,
    [employee_id] INT NULL,
    [order_date] DATETIME NOT NULL DEFAULT (GETDATE()),
    [total_amount] DECIMAL(10,2) NULL,
    [shipping_fee] DECIMAL(10,2) NULL,
    [order_status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Order_Status] DEFAULT 'pending'
        CONSTRAINT [CK_Order_Status] CHECK ([order_status] IN ('pending','processing','shipping','delivered','cancelled')),
    [shipping_address] NVARCHAR(255) NOT NULL,
    [shipping_option] NVARCHAR(50) NOT NULL,
    [note] NVARCHAR(MAX) NULL,
    [estimated_delivery_date] DATE NULL,
    CONSTRAINT [FK_Order_Customer] FOREIGN KEY ([customer_id]) REFERENCES [Customer]([customer_id]),
    CONSTRAINT [FK_Order_Employee] FOREIGN KEY ([employee_id]) REFERENCES [Employee]([employee_id])
);
GO

-- 20. Table PaymentMethod
CREATE TABLE [PaymentMethod] (
    [payment_method_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [method_name] VARCHAR(50) NOT NULL UNIQUE,
    [description] NVARCHAR(MAX) NULL
);
GO

-- 21. Table CustomerPaymentMethod
CREATE TABLE [CustomerPaymentMethod] (
    [customer_payment_method_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [customer_id] INT NOT NULL,
    [payment_method_id] INT NOT NULL,
    [provider] VARCHAR(50) NULL,
    [account_number] VARCHAR(50) NULL,
    [token] VARCHAR(255) NULL,
    [is_default] BIT NOT NULL DEFAULT 0,
    CONSTRAINT [FK_CustomerPaymentMethod_Customer] FOREIGN KEY ([customer_id]) REFERENCES [Customer]([customer_id]),
    CONSTRAINT [FK_CustomerPaymentMethod_PaymentMethod] FOREIGN KEY ([payment_method_id]) REFERENCES [PaymentMethod]([payment_method_id]),
    CONSTRAINT [UQ_CustomerPaymentMethod] UNIQUE ([customer_id], [payment_method_id], [account_number])
);
GO

-- 22. Table Payment
CREATE TABLE [Payment] (
    [payment_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [payment_method_id] INT NOT NULL,
    [order_id] INT NOT NULL,
    [payment_date] DATETIME NOT NULL,
    [amount] DECIMAL(10,2) NOT NULL,
    [payment_status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Payment_Status] DEFAULT 'pending'
        CONSTRAINT [CK_Payment_Status] CHECK ([payment_status] IN ('pending','completed','failed','refunded')),
    [transaction_id] VARCHAR(50) NULL,
    [note] NVARCHAR(MAX) NULL,
    [currency] VARCHAR(10) NOT NULL DEFAULT 'VND',
    CONSTRAINT [FK_Payment_Order] FOREIGN KEY ([order_id]) REFERENCES [Order]([order_id])
);
GO

-- 23. Table Shipment
CREATE TABLE [Shipment] (
    [shipment_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [order_id] INT NOT NULL,
    [shipping_provider] VARCHAR(50) NULL,
    [tracking_number] VARCHAR(50) NULL,
    [shipment_status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Shipment_Status] DEFAULT 'pending'
        CONSTRAINT [CK_Shipment_Status] CHECK ([shipment_status] IN ('pending','in_transit','delivered','failed')),
    [shipping_cost] DECIMAL(10,2) NULL,
    [shipping_date] DATE NULL,
    [estimated_delivery_date] DATE NULL,
    [delivery_date] DATE NULL,
    CONSTRAINT [FK_Shipment_Order] FOREIGN KEY ([order_id]) REFERENCES [Order]([order_id])
);
GO

-- 24. Table OrderItem
CREATE TABLE [OrderItem] (
    [order_item_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [order_id] INT NOT NULL,
    [product_variant_id] INT NOT NULL,
    [quantity] INT NOT NULL,
    [unit_price] DECIMAL(10,2) NOT NULL,
    [note] NVARCHAR(255) NULL,
    CONSTRAINT [FK_OrderItem_Order] FOREIGN KEY ([order_id]) REFERENCES [Order]([order_id]),
    CONSTRAINT [FK_OrderItem_ProductVariant] FOREIGN KEY ([product_variant_id]) REFERENCES [ProductVariant]([product_variant_id]),
    CONSTRAINT [UQ_OrderItem] UNIQUE ([order_id], [product_variant_id])
);
GO

-- 25. Table OrderPromotion
CREATE TABLE [OrderPromotion] (
    [order_promotion_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [order_id] INT NOT NULL,
    [promotion_id] INT NOT NULL,
    [discount_amount] DECIMAL(10,2) NULL,
    [note] NVARCHAR(MAX) NULL,
    CONSTRAINT [FK_OrderPromotion_Order] FOREIGN KEY ([order_id]) REFERENCES [Order]([order_id]),
    CONSTRAINT [FK_OrderPromotion_Promotion] FOREIGN KEY ([promotion_id]) REFERENCES [Promotion]([promotion_id]),
    CONSTRAINT [UQ_OrderPromotion] UNIQUE ([order_id], [promotion_id])
);
GO

-- 26. Table Conversation
CREATE TABLE [Conversation] (
    [conversation_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [start_time] DATETIME NOT NULL DEFAULT (GETDATE()),
    [end_time] DATETIME NULL,
    [status] VARCHAR(20) NOT NULL
        CONSTRAINT [DF_Conversation_Status] DEFAULT 'active'
        CONSTRAINT [CK_Conversation_Status] CHECK ([status] IN ('active','done')),
    [rating] VARCHAR(20) NULL
        CONSTRAINT [CK_Conversation_Rating] CHECK ([rating] IN ('bad','average','good') OR [rating] IS NULL),
    [channel] VARCHAR(50) NULL
);
GO

-- 27. Table ChatMessage
CREATE TABLE [ChatMessage] (
    [chat_message_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [conversation_id] INT NOT NULL,
    [sender_id] INT NULL,
    [sender_type] VARCHAR(20) NOT NULL
        CONSTRAINT [CK_ChatMessage_SenderType] CHECK ([sender_type] IN ('employee','customer','bot')),
    [receiver_id] INT NULL,
    [receiver_type] VARCHAR(20) NOT NULL
        CONSTRAINT [CK_ChatMessage_ReceiverType] CHECK ([receiver_type] IN ('employee','customer','bot')),
    [content] NVARCHAR(MAX) NOT NULL,
    [timestamp] DATETIME NOT NULL DEFAULT (GETDATE()),
    CONSTRAINT [FK_ChatMessage_Conversation] FOREIGN KEY ([conversation_id]) REFERENCES [Conversation]([conversation_id])
);
GO
