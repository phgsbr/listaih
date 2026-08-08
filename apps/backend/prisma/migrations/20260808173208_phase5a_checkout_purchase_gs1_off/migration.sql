-- CreateEnum
CREATE TYPE "PaymentMethod" AS ENUM ('DEBITO', 'CREDITO', 'VR', 'VA', 'DINHEIRO', 'PIX');

-- CreateEnum
CREATE TYPE "ListType" AS ENUM ('RECORRENTE', 'PONTUAL', 'MODELO');

-- CreateEnum
CREATE TYPE "ReceiptStatus" AS ENUM ('PENDING', 'PROCESSING', 'PARSED', 'FAILED', 'NOT_PROVIDED');

-- AlterTable
ALTER TABLE "ListItem" ADD COLUMN     "barcode" TEXT,
ADD COLUMN     "barcodeData" JSONB,
ADD COLUMN     "offData" JSONB;

-- AlterTable
ALTER TABLE "ShoppingList" ADD COLUMN     "completedAt" TIMESTAMP(3),
ADD COLUMN     "grocyAssociated" BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN     "listType" "ListType" NOT NULL DEFAULT 'PONTUAL';

-- AlterTable
ALTER TABLE "SystemConfig" ADD COLUMN     "aiApiKey" TEXT,
ADD COLUMN     "aiEnabled" BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN     "aiEndpoint" TEXT,
ADD COLUMN     "aiModel" TEXT,
ADD COLUMN     "aiProvider" TEXT;

-- CreateTable
CREATE TABLE "Purchase" (
    "id" TEXT NOT NULL,
    "listId" TEXT NOT NULL,
    "householdId" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "date" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "totalAmount" DOUBLE PRECISION,
    "paymentMethod" "PaymentMethod",
    "notes" TEXT,
    "receiptPhoto" TEXT,
    "receiptParsed" JSONB,
    "receiptStatus" "ReceiptStatus" NOT NULL DEFAULT 'NOT_PROVIDED',
    "itemCount" INTEGER NOT NULL,
    "items" JSONB NOT NULL,
    "grocySynced" BOOLEAN NOT NULL DEFAULT false,
    "grocySyncedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Purchase_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "Purchase_householdId_idx" ON "Purchase"("householdId");

-- CreateIndex
CREATE INDEX "Purchase_listId_idx" ON "Purchase"("listId");

-- CreateIndex
CREATE INDEX "Purchase_userId_idx" ON "Purchase"("userId");

-- AddForeignKey
ALTER TABLE "Purchase" ADD CONSTRAINT "Purchase_listId_fkey" FOREIGN KEY ("listId") REFERENCES "ShoppingList"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Purchase" ADD CONSTRAINT "Purchase_householdId_fkey" FOREIGN KEY ("householdId") REFERENCES "Household"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Purchase" ADD CONSTRAINT "Purchase_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;
