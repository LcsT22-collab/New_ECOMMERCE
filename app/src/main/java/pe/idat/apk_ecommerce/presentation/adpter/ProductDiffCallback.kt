package pe.idat.apk_ecommerce.presentation.adapter

import androidx.recyclerview.widget.DiffUtil
import pe.idat.apk_ecommerce.domain.model.Product

class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Product, newItem: Product) =
        oldItem == newItem
}