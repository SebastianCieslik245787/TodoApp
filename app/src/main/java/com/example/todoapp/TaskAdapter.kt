import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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
        val planedDate: TextView = itemView.findViewById(R.id.taskItemPlanedDateAndTime)
        val notificationIcon: ImageView = itemView.findViewById(R.id.taskItemNotificationIcon)
        val attachmentIcon: ImageView = itemView.findViewById(R.id.taskItemAttachmentIcon)
        val doneIcon: ImageView = itemView.findViewById(R.id.doneIcon)
        val endDate: TextView = itemView.findViewById(R.id.taskItemEndDataAndTime)
        val category: TextView = itemView.findViewById(R.id.taskItemCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.planedDate.text = "Zaplanowano na: ${task.planedDate} ${task.planedTime}"
        holder.category.text = task.category


        holder.notificationIcon.visibility = if (!task.notificationDate.isNullOrBlank() || !task.notificationTime.isNullOrBlank()) {
            VISIBLE
        } else {
            GONE
        }

        holder.attachmentIcon.visibility = if (task.hasAttachments) VISIBLE else GONE

        if(task.isDone){
            holder.doneIcon.visibility = VISIBLE
            holder.endDate.text = "${task.endDate} ${task.endTime}"
            holder.endDate.visibility = VISIBLE
        }

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
