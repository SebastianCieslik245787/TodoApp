import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.Task

class TaskAdapter(private val tasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.taskItemTitle)
        val createdDate: TextView = itemView.findViewById(R.id.taskItemCreatedDateAndTime)
        val notificationIcon: ImageView = itemView.findViewById(R.id.taskItemNotificationIcon)
        val attachmentIcon: ImageView = itemView.findViewById(R.id.taskItemAttachmentIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.createdDate.text = "Data utworzenia: ${task.createDate}"

        holder.notificationIcon.visibility = if (!task.notificationDate.isNullOrBlank() || !task.notificationTime.isNullOrBlank()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.attachmentIcon.visibility = if (task.hasAttachments) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = tasks.size
}
