-- V9: Add Admin Password Reset Token Table
CREATE TABLE [AdminPasswordResetToken] (
    [token_id] INT NOT NULL IDENTITY(1,1) PRIMARY KEY,
    [employee_id] INT NOT NULL,
    [token] VARCHAR(255) NOT NULL UNIQUE,
    [expires_at] DATETIME NOT NULL,
    [created_at] DATETIME NOT NULL DEFAULT (GETDATE()),
    [used_at] DATETIME NULL,
    [is_used] BIT NOT NULL DEFAULT 0,
    
    CONSTRAINT [FK_AdminPasswordResetToken_Employee] FOREIGN KEY ([employee_id]) REFERENCES [Employee]([employee_id])
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    CONSTRAINT [CK_AdminPasswordResetToken_ExpiresAt_Valid] CHECK ([expires_at] > [created_at]),
    CONSTRAINT [CK_AdminPasswordResetToken_UsedAt_Valid] CHECK ([used_at] IS NULL OR [used_at] >= [created_at])
);
GO

-- Create index for performance
CREATE INDEX [IX_AdminPasswordResetToken_Token] ON [AdminPasswordResetToken]([token]);
CREATE INDEX [IX_AdminPasswordResetToken_Employee_Active] ON [AdminPasswordResetToken]([employee_id], [is_used], [expires_at]);
GO 