-- AlterTable
ALTER TABLE "SystemConfig" ADD COLUMN     "apiBaseUrl" TEXT,
ADD COLUMN     "apiEnabled" BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN     "apiKey" TEXT;
