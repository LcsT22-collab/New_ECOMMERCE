package pe.idat.apk_ecommerce.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import pe.idat.apk_ecommerce.R
import pe.idat.apk_ecommerce.databinding.ItemProductBinding
import pe.idat.apk_ecommerce.domain.model.Product
import pe.idat.apk_ecommerce.util.CartManager


class ProductAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onAddToCart: (Product, Int) -> Unit,
    private val onToggleFavorite: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(ProductDiffCallback()) {

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductTitle.text = product.name
            binding.tvProductPrice.text = String.format("S/. %.2f", product.price)
            binding.tvCategoryBadge.text = product.category

            val cartItem = CartManager.getCartItem(product.id)
            val inCart = cartItem?.quantity ?: 0
            val availableStock = product.stock - inCart

            binding.tvStockInfo.text = "Stock: $availableStock"
            if (inCart > 0) {
                binding.tvStockInfo.append(" (${inCart} en carrito)")
            }

            binding.btnFavorite.setImageResource(
                if (product.isFavorite) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            binding.btnFavorite.setOnClickListener { onToggleFavorite(product) }
            binding.root.setOnClickListener { onItemClick(product) }

            var quantity = 1
            binding.tvQuantity.text = quantity.toString()

            binding.btnDecrease.setOnClickListener {
                if (quantity > 1) {
                    quantity--
                    binding.tvQuantity.text = quantity.toString()
                }
            }

            binding.btnIncrease.setOnClickListener {
                if (quantity < availableStock) {
                    quantity++
                    binding.tvQuantity.text = quantity.toString()
                } else {
                    showToast("Sin stock disponible")
                }
            }

            binding.btnAddToCart.setOnClickListener {
                if (availableStock >= quantity) {
                    onAddToCart(product, quantity)
                } else {
                    showToast("Stock insuficiente")
                }
            }

            binding.ivProduct.load(product.image)
        }

        private fun showToast(message: String) {
            Toast.makeText(itemView.context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}