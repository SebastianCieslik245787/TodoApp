import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.Task
import androidx.core.content.edit
import com.example.todoapp.ShowTask

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
        holder.createdDate.text = "${task.planedDate} ${task.planedTime}"

        holder.notificationIcon.visibility = if (!task.notificationDate.isNullOrBlank() || !task.notificationTime.isNullOrBlank()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        holder.attachmentIcon.visibility = if (task.hasAttachments) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val sharedPref = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE)
            sharedPref.edit { putInt("selected_task_id", task.id) }
            val intent = Intent(context, ShowTask::class.java)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tasks.size
}
