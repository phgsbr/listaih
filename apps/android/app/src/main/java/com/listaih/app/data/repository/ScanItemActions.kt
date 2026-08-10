package com.listaih.app.data.repository

import com.listaih.app.data.local.entity.ListItemEntity
import com.listaih.app.data.network.model.UpdateItemRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backend actions behind the post-scan popups (ANDROID-PLAN Fase 6, 6.4-6.6).
 */
@Singleton
class ScanItemActions @Inject constructor(
    private val repository: ShoppingRepository,
    private val productRepository: ProductRepository,
) {

    /** Mark an item as bought with the given quantity (6.2 confirm / 6.8 auto-confirm). */
    suspend fun confirm(listId: String, itemId: String, quantity: Double): Result<Unit> {
        return repository.updateItem(
            itemId, listId,
            UpdateItemRequest(
                name = null,
                quantity = quantity,
                unit = null,
                estimatedPrice = null,
                category = null,
                checked = true,
                position = null,
                barcode = null,
                barcodeRaw = null,
                productId = null,
            )
        )
    }

    /** Increase the item quantity (6.7 — repeated scan of the same code). */
    suspend fun increment(listId: String, itemId: String, quantity: Double): Result<Unit> {
        return repository.updateItem(
            itemId, listId,
            UpdateItemRequest(
                name = null,
                quantity = quantity,
                unit = null,
                estimatedPrice = null,
                category = null,
                checked = null,
                position = null,
                barcode = null,
                barcodeRaw = null,
                productId = null,
            )
        )
    }

    /**
     * "Associar à lista" (6.4): creates a Product with the item's name/barcode and
     * links barcode + productId to the existing list item
     * ("Manteiga Pikachu = manteiga Sonic").
     */
    suspend fun associateBarcode(
        listId: String,
        itemId: String,
        barcode: String,
        itemName: String,
        itemCategory: String?,
    ): Result<Unit> {
        val created = productRepository.create(itemName, barcode, itemCategory).getOrNull()
        val product = created ?: productRepository.lookup(barcode).getOrNull()
        if (product == null) {
            return Result.failure(Exception("Nao foi possivel cadastrar o produto"))
        }
        return repository.updateItem(
            itemId, listId,
            UpdateItemRequest(
                name = null,
                quantity = null,
                unit = null,
                estimatedPrice = null,
                category = null,
                checked = null,
                position = null,
                barcode = barcode,
                barcodeRaw = null,
                productId = product.id,
            )
        )
    }

    /**
     * "Cadastrar novo" (6.5): Product (existing via OFF lookup or created) +
     * ListItem on the list, already checked.
     */
    suspend fun createScannedItem(
        listId: String,
        barcode: String,
        name: String,
        checked: Boolean = true,
    ): Result<ListItemEntity> {
        val existing = productRepository.lookup(barcode).getOrNull()
        val product = existing ?: productRepository
            .create(name.ifBlank { "Produto $barcode" }, barcode, null)
            .getOrNull()
            ?: productRepository.lookup(barcode).getOrNull()

        val itemName = product?.name ?: name.ifBlank { "Produto $barcode" }
        val itemResult = repository.createItem(
            listId = listId,
            name = itemName,
            quantity = 1.0,
            unit = "unit",
            estimatedPrice = null,
            category = product?.category,
            barcode = barcode,
            barcodeRaw = barcode,
            productId = product?.id,
        )
        if (itemResult.isSuccess && checked) {
            val itemId = itemResult.getOrThrow().id
            repository.updateItem(
                itemId, listId,
                UpdateItemRequest(
                    name = null,
                    quantity = null,
                    unit = null,
                    estimatedPrice = null,
                    category = null,
                    checked = true,
                    position = null,
                    barcode = null,
                    barcodeRaw = null,
                    productId = null,
                )
            )
        }
        return itemResult
    }

    /** "Genérico" (6.6): creates "Produto [código]" without details. */
    suspend fun createGenericItem(listId: String, barcode: String): Result<ListItemEntity> {
        return createScannedItem(listId, barcode, "Produto $barcode", checked = true)
    }

    /** Suggests a name from the catalog/OFF for the "Cadastrar novo" dialog. */
    suspend fun suggestName(barcode: String): String? {
        return productRepository.lookup(barcode).getOrNull()?.name
    }
}