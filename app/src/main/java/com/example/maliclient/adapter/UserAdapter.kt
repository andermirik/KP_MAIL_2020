package com.example.maliclient.adapter

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import com.example.maliclient.MainActivity
import com.example.maliclient.R
import com.example.maliclient.model.UserCard
import kotlin.random.Random


class UserAdapter(var context: Context, var users: List<UserCard>, var main_activity : MainActivity)
    : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    var recyclerView: RecyclerView? = null
    var selected_user_pos = 0

    var colors = arrayOf(
        Color.parseColor("#F44336"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#3F51B5"),
        Color.parseColor("#009688"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#FF5722"),
        Color.parseColor("#EF6C00"),
        Color.parseColor("#D84315"),
        Color.parseColor("#37474F")
    )
    var random = Random(0xff2244)

    fun select(num : Int){
        if (num == RecyclerView.NO_POSITION)
            return

        notifyItemChanged(selected_user_pos)
        selected_user_pos = num
        notifyItemChanged(selected_user_pos)

        if(recyclerView!= null)
            scrollToCenter(recyclerView!!.layoutManager as LinearLayoutManager, recyclerView!!, num)
        notifyDataSetChanged()
        main_activity.on_select_user(users, num)
    }

    fun add_new_user(){
        main_activity.on_add_new_user()
    }

    inner class ViewHolder(item_view: View) : RecyclerView.ViewHolder(item_view) {
        var tv_userlogin : TextView = item_view.findViewById(R.id.tv_userlogin)
        var inner_circle : CardView = item_view.findViewById(R.id.inner_circle)
        var tv_userlogin_big : TextView = item_view.findViewById(R.id.tv_userlogin_big)
        var inner_circle_big : CardView = item_view.findViewById(R.id.inner_circle_big)
        var background_circle : CardView = item_view.findViewById(R.id.background_circle)
        var border_circle : CardView = item_view.findViewById(R.id.border_circle)
        var color = colors.random(random)

        var image_add : CardView = item_view.findViewById(R.id.image_add)

        init{
            inner_circle.setOnClickListener{
                select(adapterPosition)
            }
            image_add.setOnClickListener{
                add_new_user()
            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view : View = LayoutInflater.from(context).inflate(R.layout.nav_profile_item, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return users.count()
    }


    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = users[position]

        if(user.is_add){
            holder.image_add.visibility = View.VISIBLE
            holder.inner_circle.visibility = View.GONE
            holder.setIsRecyclable(false)
        }else {
            val name = user.login.split("@")[0]
            val login_text = (name[0].toString() + name[name.length - 1]).toUpperCase()

            holder.inner_circle.tag = position
            val color_num = Math.abs(user.login.hashCode() + user.login[0].toInt()) % colors.size
            holder.inner_circle.setCardBackgroundColor(colors[color_num])
            holder.inner_circle_big.setCardBackgroundColor(colors[color_num])

            if (position == selected_user_pos) {
                holder.border_circle.visibility = View.VISIBLE
                holder.background_circle.visibility = View.VISIBLE
                holder.inner_circle_big.visibility = View.VISIBLE
                holder.inner_circle.visibility = View.GONE
                holder.tv_userlogin_big.text = login_text
                holder.setIsRecyclable(false)

            } else {
                holder.tv_userlogin.text = login_text
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    fun scrollToCenter(
        layoutManager: LinearLayoutManager,
        recyclerList: RecyclerView,
        clickPosition: Int
    ) {
        val smoothScroller: SmoothScroller? = createSnapScroller(recyclerList, layoutManager)
        if (smoothScroller != null) {
            smoothScroller.targetPosition = clickPosition
            layoutManager.startSmoothScroll(smoothScroller)
        }
    }

    // This number controls the speed of smooth scroll
    private val MILLISECONDS_PER_INCH = 70f

    private val DIMENSION = 2
    private val HORIZONTAL = 0
    private val VERTICAL = 1

    @Nullable
    private fun createSnapScroller(
        mRecyclerView: RecyclerView,
        layoutManager: RecyclerView.LayoutManager
    ): LinearSmoothScroller? {
        return if (layoutManager !is ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistances =
                    calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[HORIZONTAL]
                val dy = snapDistances[VERTICAL]
                val time = calculateTimeForDeceleration(
                    Math.max(
                        Math.abs(dx),
                        Math.abs(dy)
                    )
                )
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }
    }


    private fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(DIMENSION)
        if (layoutManager.canScrollHorizontally()) {
            out[HORIZONTAL] = distanceToCenter(
                layoutManager, targetView,
                OrientationHelper.createHorizontalHelper(layoutManager)
            )
        }
        if (layoutManager.canScrollVertically()) {
            out[VERTICAL] = distanceToCenter(
                layoutManager, targetView,
                OrientationHelper.createHorizontalHelper(layoutManager)
            )
        }
        return out
    }


    private fun distanceToCenter(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View, helper: OrientationHelper
    ): Int {
        val childCenter = (helper.getDecoratedStart(targetView)
                + helper.getDecoratedMeasurement(targetView) / 2)
        val containerCenter: Int
        containerCenter = if (layoutManager.clipToPadding) {
            helper.startAfterPadding + helper.totalSpace / 2
        } else {
            helper.end / 2
        }
        return childCenter - containerCenter
    }

}