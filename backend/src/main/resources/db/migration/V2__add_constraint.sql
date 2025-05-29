-- 1. Table Employee
ALTER TABLE [Employee]
ADD
    CONSTRAINT [CK_Employee_Email_Format] CHECK (
        [email] IS NULL
        OR ([email] LIKE '%_@__%.__%' AND CHARINDEX(' ', [email]) = 0)
    ),
    CONSTRAINT [CK_Employee_PhoneNumber_Format] CHECK (
        LEN(LTRIM(RTRIM([phone_number]))) > 0
        AND [phone_number] NOT LIKE '%[^0-9 ()+-]%'
    ),
    CONSTRAINT [CK_Employee_FullName_NotEmpty] CHECK (
        LEN(LTRIM(RTRIM([full_name]))) > 0
    ),
    CONSTRAINT [CK_Employee_Address_NotEmpty] CHECK (
        LEN(LTRIM(RTRIM([address]))) > 0
    ),
    CONSTRAINT [CK_Employee_StartDate_Valid] CHECK (
        [start_date] IS NULL OR [start_date] <= CAST(GETDATE() AS DATE)
    ),
    CONSTRAINT [CK_Employee_DOB_Valid] CHECK (
        [date_of_birth] IS NULL OR [date_of_birth] < CAST(GETDATE() AS DATE)
    ),
    CONSTRAINT [CK_Employee_LastLogin_Valid] CHECK (
        [last_login] IS NULL OR ([start_date] IS NOT NULL AND [last_login] >= [start_date])
    );
GO

-- 2. Table Role
ALTER TABLE [Role]
ADD
    CONSTRAINT [CK_Role_RoleName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([role_name]))) > 0);
GO
CREATE UNIQUE INDEX [UQ_Role_IsDefault_True] ON [Role]([is_default]) WHERE [is_default] = 1;
GO

-- 4. Table Permission
ALTER TABLE [Permission]
ADD
    CONSTRAINT [CK_Permission_PermissionName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([permission_name]))) > 0);
GO

-- 6. Table Material
ALTER TABLE [Material]
ADD
    CONSTRAINT [CK_Material_MaterialName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([material_name]))) > 0),
    CONSTRAINT [CK_Material_Unit_NotEmpty] CHECK (LEN(LTRIM(RTRIM([unit]))) > 0),
    CONSTRAINT [CK_Material_QuantityInStock_NonNegative] CHECK ([quantity_in_stock] >= 0),
    CONSTRAINT [CK_Material_ReorderLevel_NonNegative] CHECK ([reorder_level] IS NULL OR [reorder_level] >= 0),
    CONSTRAINT [CK_Material_Price_NonNegative] CHECK ([price] >= 0);
GO

-- 7. Table MaterialTransaction
ALTER TABLE [MaterialTransaction]
ADD
    CONSTRAINT [CK_MaterialTransaction_BeforeQuantity_NonNegative] CHECK ([before_quantity] IS NULL OR [before_quantity] >= 0),
    CONSTRAINT [CK_MaterialTransaction_AfterQuantity_NonNegative] CHECK ([after_quantity] IS NULL OR [after_quantity] >= 0),
    CONSTRAINT [CK_MaterialTransaction_CostPrice_NonNegative] CHECK ([cost_price] IS NULL OR [cost_price] >= 0),
    CONSTRAINT [CK_MaterialTransaction_Date_Valid] CHECK ([transaction_date] <= GETDATE());
GO

-- 8. Table Brand
ALTER TABLE [Brand]
ADD
    CONSTRAINT [CK_Brand_BrandName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([brand_name]))) > 0),
    CONSTRAINT [CK_Brand_WebsiteUrl_Format] CHECK ([website_url] IS NULL OR [website_url] LIKE 'http://%' OR [website_url] LIKE '%');
GO

-- 9. Table Product
ALTER TABLE [Product]
ADD
    CONSTRAINT [CK_Product_ProductName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([product_name]))) > 0);
GO

-- 10. Table ProductVariant
ALTER TABLE [ProductVariant]
ADD
    CONSTRAINT [CK_ProductVariant_Volume_Positive] CHECK ([volume] > 0),
    CONSTRAINT [CK_ProductVariant_Price_NonNegative] CHECK ([price] >= 0),
    CONSTRAINT [CK_ProductVariant_DiscountPrice_Valid] CHECK ([discount_price] IS NULL OR ([discount_price] >= 0 AND [discount_price] <= [price])),
    CONSTRAINT [CK_ProductVariant_QuantityInStock_NonNegative] CHECK ([quantity_in_stock] >= 0),
    CONSTRAINT [CK_ProductVariant_ReorderLevel_NonNegative] CHECK ([reorder_level] IS NULL OR [reorder_level] >= 0);
GO

-- 11. Table InventoryTransaction
ALTER TABLE [InventoryTransaction]
ADD
    CONSTRAINT [CK_InventoryTransaction_Quantity_Positive] CHECK ([quantity] > 0),
    CONSTRAINT [CK_InventoryTransaction_BeforeQuantity_NonNegative] CHECK ([before_quantity] IS NULL OR [before_quantity] >= 0),
    CONSTRAINT [CK_InventoryTransaction_AfterQuantity_NonNegative] CHECK ([after_quantity] IS NULL OR [after_quantity] >= 0),
    CONSTRAINT [CK_InventoryTransaction_CostPrice_NonNegative] CHECK ([cost_price] IS NULL OR [cost_price] >= 0),
    CONSTRAINT [CK_InventoryTransaction_Date_Valid] CHECK ([transaction_date] <= GETDATE());
GO

-- 12. Table ProductDetail
ALTER TABLE [ProductDetail]
ADD
    CONSTRAINT [CK_ProductDetail_DetailValue_NotEmpty] CHECK (LEN(LTRIM(RTRIM([detail_value]))) > 0);
GO

-- 13. Table Promotion
ALTER TABLE [Promotion]
ADD
    CONSTRAINT [CK_Promotion_PromotionName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([promotion_name]))) > 0),
    CONSTRAINT [CK_Promotion_EndDate_Valid] CHECK ([end_date] IS NULL OR [end_date] >= [start_date]),
    CONSTRAINT [CK_Promotion_DiscountValue_Valid] CHECK (
        [discount_value] IS NULL OR
        ([discount_value] >= 0 AND ([discount_type] != 'percentage' OR [discount_value] <= 100))
    ),
    CONSTRAINT [CK_Promotion_UsageLimit_NonNegative] CHECK ([usage_limit] IS NULL OR [usage_limit] >= 0);
GO

-- 14. Table ProductPromotion
ALTER TABLE [ProductPromotion]
ADD
    CONSTRAINT [CK_ProductPromotion_EndDate_Valid] CHECK ([end_date] IS NULL OR [end_date] >= [start_date]),
    CONSTRAINT [CK_ProductPromotion_MaxDiscountAmount_NonNegative] CHECK ([max_discount_amount] IS NULL OR [max_discount_amount] >= 0);
GO

-- 15. Table Customer
ALTER TABLE [Customer]
ADD
    CONSTRAINT [CK_Customer_Name_NotEmpty] CHECK (LEN(LTRIM(RTRIM([name]))) > 0),
    CONSTRAINT [CK_Customer_Email_Format] CHECK ([email] IS NULL OR [email] LIKE '%_@__%.__%'),
    CONSTRAINT [CK_Customer_PhoneNumber_Format] CHECK ([phone_number] IS NULL OR (LEN([phone_number]) > 0 AND [phone_number] NOT LIKE '%[^0-9 ()+-]%')),
    CONSTRAINT [CK_Customer_Rating_Range] CHECK ([rating] >= 0),
    CONSTRAINT [CK_Customer_LoyaltyPoints_NonNegative] CHECK ([loyalty_points] >= 0),
    CONSTRAINT [CK_Customer_UpdateAt_Valid] CHECK ([update_at] IS NULL OR [update_at] >= [create_at]);
GO

-- 16. Table Cart
ALTER TABLE [Cart]
ADD
    CONSTRAINT [CK_Cart_TotalAmount_NonNegative] CHECK ([total_amount] IS NULL OR [total_amount] >= 0);
GO

-- 17. Table CartItem
ALTER TABLE [CartItem]
ADD
    CONSTRAINT [CK_CartItem_Quantity_Positive] CHECK ([quantity] > 0),
    CONSTRAINT [CK_CartItem_UnitPrice_NonNegative] CHECK ([unit_price] >= 0);
GO

-- 18. Table Wishlist
ALTER TABLE [Wishlist]
ADD
    CONSTRAINT [CK_Wishlist_AddedDate_Valid] CHECK ([added_date] <= GETDATE());
GO

-- 19. Table Order
ALTER TABLE [Order]
ADD
    CONSTRAINT [CK_Order_TotalAmount_NonNegative] CHECK ([total_amount] IS NULL OR [total_amount] >= 0),
    CONSTRAINT [CK_Order_ShippingFee_NonNegative] CHECK ([shipping_fee] IS NULL OR [shipping_fee] >= 0),
    CONSTRAINT [CK_Order_ShippingAddress_NotEmpty] CHECK (LEN(LTRIM(RTRIM([shipping_address]))) > 0),
    CONSTRAINT [CK_Order_ShippingOption_NotEmpty] CHECK (LEN(LTRIM(RTRIM([shipping_option]))) > 0),
    CONSTRAINT [CK_Order_OrderDate_Valid] CHECK ([order_date] <= GETDATE()),
    CONSTRAINT [CK_Order_EstDeliveryDate_Valid] CHECK ([estimated_delivery_date] IS NULL OR [estimated_delivery_date] >= CAST([order_date] AS DATE));
GO

-- 20. Table PaymentMethod
ALTER TABLE [PaymentMethod]
ADD
    CONSTRAINT [CK_PaymentMethod_MethodName_NotEmpty] CHECK (LEN(LTRIM(RTRIM([method_name]))) > 0);
GO

-- 21. Table CustomerPaymentMethod
CREATE UNIQUE INDEX [UQ_CustomerPaymentMethod_IsDefault_True] ON [CustomerPaymentMethod]([customer_id], [is_default]) WHERE [is_default] = 1;
GO

-- 22. Table Payment
ALTER TABLE [Payment]
ADD
    CONSTRAINT [CK_Payment_PaymentDate_Valid] CHECK ([payment_date] <= GETDATE()),
    CONSTRAINT [CK_Payment_Currency_NotEmpty] CHECK (LEN(LTRIM(RTRIM([currency]))) > 0);
GO

-- 23. Table Shipment
ALTER TABLE [Shipment]
ADD
    CONSTRAINT [CK_Shipment_ShippingCost_NonNegative] CHECK ([shipping_cost] IS NULL OR [shipping_cost] >= 0),
    CONSTRAINT [CK_Shipment_ShippingDate_Valid] CHECK ([shipping_date] IS NULL OR [shipping_date] <= CAST(GETDATE() AS DATE)),
    CONSTRAINT [CK_Shipment_EstDeliveryDate_Valid] CHECK ([estimated_delivery_date] IS NULL OR [shipping_date] IS NULL OR [estimated_delivery_date] >= [shipping_date]),
    CONSTRAINT [CK_Shipment_DeliveryDate_Valid] CHECK ([delivery_date] IS NULL OR [shipping_date] IS NULL OR [delivery_date] >= [shipping_date]);
GO

-- 24. Table OrderItem
ALTER TABLE [OrderItem]
ADD
    CONSTRAINT [CK_OrderItem_Quantity_Positive] CHECK ([quantity] > 0),
    CONSTRAINT [CK_OrderItem_UnitPrice_NonNegative] CHECK ([unit_price] >= 0);
GO

-- 25. Table OrderPromotion
ALTER TABLE [OrderPromotion]
ADD
    CONSTRAINT [CK_OrderPromotion_DiscountAmount_NonNegative] CHECK ([discount_amount] IS NULL OR [discount_amount] >= 0);
GO

-- 26. Table Conversation
ALTER TABLE [Conversation]
ADD
    CONSTRAINT [CK_Conversation_EndTime_Valid] CHECK ([end_time] IS NULL OR [end_time] >= [start_time]),
    CONSTRAINT [CK_Conversation_StatusEndTime_Logic] CHECK ([status] != 'done' OR [end_time] IS NOT NULL),
    CONSTRAINT [CK_Conversation_RatingStatus_Logic] CHECK ([rating] IS NULL OR [status] = 'done');
GO

-- 27. Table ChatMessage
ALTER TABLE [ChatMessage]
ADD
    CONSTRAINT [CK_ChatMessage_Content_NotEmpty] CHECK (LEN(LTRIM(RTRIM([content]))) > 0),
    CONSTRAINT [CK_ChatMessage_Timestamp_Valid] CHECK ([timestamp] <= GETDATE());
GO