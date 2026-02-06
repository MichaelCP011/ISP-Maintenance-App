import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.isp_icon.R

class DashboardAdapter(
    private val menuList: List<DashboardMenu>,
    private val onItemClick: (DashboardMenu) -> Unit // Agar bisa diklik
) : RecyclerView.Adapter<DashboardAdapter.MenuViewHolder>() {

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvMenuTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvMenuDesc)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivMenuIcon)
        val cardBg: CardView = itemView.findViewById(R.id.cardIconBg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]

        holder.tvTitle.text = menu.title
        holder.tvDesc.text = menu.description
        holder.ivIcon.setImageResource(menu.iconRes)

        // Handle Klik
        holder.itemView.setOnClickListener {
            onItemClick(menu)
        }
    }

    override fun getItemCount(): Int = menuList.size
}