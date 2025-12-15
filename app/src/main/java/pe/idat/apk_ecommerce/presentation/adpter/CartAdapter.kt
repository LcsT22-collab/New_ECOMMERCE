package pe.idat.apk_ecommerce.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import pe.idat.apk_ecommerce.databinding.ItemCartBinding
import pe.idat.apk_ecommerce.domain.model.Product


class CartAdapter(
    private var cartItems: List<Product> = emptyList(),
    private val onRemoveFromCart: (Product) -> Unit,
    private val onUpdateQuantity: (Product, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvCartTitle.text = product.name
            binding.tvCartPrice.text = "S/. ${product.totalPrice()}"
            binding.tvCartQuantity.text = product.quantity.toString()

            binding.ivCartProduct.load(product.image)

            binding.btnDecreaseCart.setOnClickListener {
                if (product.quantity > 1) {
                    onUpdateQuantity(product, product.quantity - 1)
                }
            }

            binding.btnIncreaseCart.setOnClickListener {
                if (product.quantity < product.stock) {
                    onUpdateQuantity(product, product.quantity + 1)
                }
            }

            binding.btnRemoveFromCart.setOnClickListener { onRemoveFromCart(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCartBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    fun updateCartItems(newCartItems: List<Product>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }
}