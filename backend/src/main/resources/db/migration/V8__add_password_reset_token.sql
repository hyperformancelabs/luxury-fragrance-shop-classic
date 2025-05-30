-- V4: Add Password Reset Token Table
CREATE TABLE [PasswordResetToken] (
    [token_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [customer_id] INT NOT NULL,
    [token] VARCHAR(255) NOT NULL UNIQUE,
    [expires_at] DATETIME NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT (GETDATE()),
    [used_at] DATETIME NULL,
    [is_used] BIT NOT NULL DEFAULT 0,
    
    CONSTRAINT [FK_PasswordResetToken_Customer] FOREIGN KEY ([customer_id]) REFERENCES [Customer]([customer_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [CK_PasswordResetToken_ExpiresAt_Valid] CHECK ([expires_at] > [created_at]),
    CONSTRAINT [CK_PasswordResetToken_UsedAt_Valid] CHECK ([used_at] IS NULL OR [used_at] >= [created_at])
);
GO

-- Create index for performance
CREATE INDEX [IX_PasswordResetToken_Token] ON [PasswordResetToken]([token]);
CREATE INDEX [IX_PasswordResetToken_Customer_Active] ON [PasswordResetToken]([customer_id], [is_used], [expires_at]);
GO 