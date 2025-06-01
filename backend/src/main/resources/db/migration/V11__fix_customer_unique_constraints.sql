-- Fix unique constraints for Customer table to properly handle NULL values

-- First, let's identify all tables that have foreign key references to Customer
-- We need to create a list of all these constraints so we can drop and recreate them

-- Create a temporary table to store foreign key constraint information
CREATE TABLE #ForeignKeyConstraints (
    ConstraintName NVARCHAR(128),
    ParentTable NVARCHAR(128),
    ParentColumn NVARCHAR(128),
    ReferencedTable NVARCHAR(128),
    ReferencedColumn NVARCHAR(128),
    DeleteRule NVARCHAR(60),
    UpdateRule NVARCHAR(60)
);

-- Gather foreign key constraints pointing to Customer
INSERT INTO #ForeignKeyConstraints
SELECT 
    fk.name AS ConstraintName,
    OBJECT_NAME(fk.parent_object_id) AS ParentTable,
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS ParentColumn,
    OBJECT_NAME(fk.referenced_object_id) AS ReferencedTable,
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS ReferencedColumn,
    CASE fk.delete_referential_action
        WHEN 0 THEN 'NO ACTION'
        WHEN 1 THEN 'CASCADE'
        WHEN 2 THEN 'SET NULL'
        WHEN 3 THEN 'SET DEFAULT'
    END AS DeleteRule,
    CASE fk.update_referential_action
        WHEN 0 THEN 'NO ACTION'
        WHEN 1 THEN 'CASCADE'
        WHEN 2 THEN 'SET NULL'
        WHEN 3 THEN 'SET DEFAULT'
    END AS UpdateRule
FROM 
    sys.foreign_keys AS fk
    INNER JOIN sys.foreign_key_columns AS fkc 
    ON fk.OBJECT_ID = fkc.constraint_object_id
WHERE 
    OBJECT_NAME(fk.referenced_object_id) = 'Customer';

-- 1. Drop foreign key constraints that reference Customer
DECLARE @fk_name NVARCHAR(128);
DECLARE @parent_table NVARCHAR(128);
DECLARE @drop_fk_sql NVARCHAR(500);

DECLARE fk_cursor CURSOR FOR
SELECT ConstraintName, ParentTable FROM #ForeignKeyConstraints;

OPEN fk_cursor;
FETCH NEXT FROM fk_cursor INTO @fk_name, @parent_table;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @drop_fk_sql = 'ALTER TABLE [' + @parent_table + '] DROP CONSTRAINT [' + @fk_name + ']';
    EXEC sp_executesql @drop_fk_sql;
    FETCH NEXT FROM fk_cursor INTO @fk_name, @parent_table;
END

CLOSE fk_cursor;
DEALLOCATE fk_cursor;

-- 2. Create a temporary table to store customer data
SELECT * INTO #CustomerTemp FROM [Customer];

-- 3. Drop the Customer table with its constraints
DROP TABLE [Customer];

-- 4. Recreate the Customer table without unique constraints
CREATE TABLE [Customer] (
    [customer_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [username] VARCHAR(50) NULL,
    [password] VARCHAR(255) NULL,
    [name] NVARCHAR(100) NOT NULL,
    [phone_number] VARCHAR(20) NULL,
    [email] VARCHAR(100) NULL,
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

-- 5. Create filtered unique indexes that ignore NULL values
CREATE UNIQUE NONCLUSTERED INDEX [IX_Customer_Username_Unique] 
ON [Customer]([username]) 
WHERE [username] IS NOT NULL;

CREATE UNIQUE NONCLUSTERED INDEX [IX_Customer_Email_Unique] 
ON [Customer]([email]) 
WHERE [email] IS NOT NULL;

CREATE UNIQUE NONCLUSTERED INDEX [IX_Customer_PhoneNumber_Unique] 
ON [Customer]([phone_number]) 
WHERE [phone_number] IS NOT NULL;

-- 6. Copy back data
SET IDENTITY_INSERT [Customer] ON;

INSERT INTO [Customer] (
    [customer_id], [username], [password], [name], [phone_number], [email],
    [street], [ward], [district], [city], [shipping_note], [note],
    [rating], [status], [loyalty_points], [create_at], [update_at]
)
SELECT 
    [customer_id], [username], [password], [name], [phone_number], [email],
    [street], [ward], [district], [city], [shipping_note], [note],
    [rating], [status], [loyalty_points], [create_at], [update_at]
FROM #CustomerTemp;

SET IDENTITY_INSERT [Customer] OFF;

-- 7. Add back the constraints from V2__add_constraint.sql for Customer table
ALTER TABLE [Customer]
ADD
    CONSTRAINT [CK_Customer_Name_NotEmpty] CHECK (LEN(LTRIM(RTRIM([name]))) > 0),
    CONSTRAINT [CK_Customer_Email_Format] CHECK ([email] IS NULL OR [email] LIKE '%_@__%.__%'),
    CONSTRAINT [CK_Customer_PhoneNumber_Format] CHECK ([phone_number] IS NULL OR (LEN([phone_number]) > 0 AND [phone_number] NOT LIKE '%[^0-9 ()+-]%')),
    CONSTRAINT [CK_Customer_Rating_Range] CHECK ([rating] >= 0),
    CONSTRAINT [CK_Customer_LoyaltyPoints_NonNegative] CHECK ([loyalty_points] >= 0),
    CONSTRAINT [CK_Customer_UpdateAt_Valid] CHECK ([update_at] IS NULL OR [update_at] >= [create_at]);

-- 8. Recreate the foreign key constraints
DECLARE @add_fk_sql NVARCHAR(1000);
DECLARE @parent_column NVARCHAR(128);
DECLARE @referenced_column NVARCHAR(128);
DECLARE @delete_rule NVARCHAR(60);
DECLARE @update_rule NVARCHAR(60);

DECLARE fk_add_cursor CURSOR FOR
SELECT 
    ConstraintName, 
    ParentTable, 
    ParentColumn, 
    ReferencedColumn, 
    DeleteRule, 
    UpdateRule 
FROM #ForeignKeyConstraints;

OPEN fk_add_cursor;
FETCH NEXT FROM fk_add_cursor INTO @fk_name, @parent_table, @parent_column, @referenced_column, @delete_rule, @update_rule;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @add_fk_sql = 'ALTER TABLE [' + @parent_table + '] ADD CONSTRAINT [' + @fk_name + '] ' +
                      'FOREIGN KEY ([' + @parent_column + ']) REFERENCES [Customer]([' + @referenced_column + ']) ' +
                      'ON DELETE ' + @delete_rule + ' ON UPDATE ' + @update_rule;
    EXEC sp_executesql @add_fk_sql;
    FETCH NEXT FROM fk_add_cursor INTO @fk_name, @parent_table, @parent_column, @referenced_column, @delete_rule, @update_rule;
END

CLOSE fk_add_cursor;
DEALLOCATE fk_add_cursor;

-- 9. Clean up
DROP TABLE #CustomerTemp;
DROP TABLE #ForeignKeyConstraints; 