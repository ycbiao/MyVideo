package com.example.ycb.myvideo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_recycler_list.view.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_main.layoutManager = GridLayoutManager(this,1)
        recycler_main.adapter = ClassRecyclerAdapter(this, arrayListOf("mediaplayer","exoplayer"))
    }

    class ClassRecyclerAdapter(var context: Context,var list: ArrayList<String>) : RecyclerView.Adapter<ClassRecyclerAdapter.ClassViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ClassViewHolder {
            return ClassViewHolder(View.inflate(context,R.layout.main_recycler_list,null))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(p0: ClassViewHolder, p1: Int) {
            p0.setData(list.get(p1))
        }

        inner class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var btn_class = itemView.btn_class
            fun setData(string: String){
                btn_class.text = string
                btn_class.setOnClickListener {

                    if(string.equals(list[0]))
                        context.startActivity(Intent(context,MyMediaPlayerActivity::class.java))
                    if(string.equals(list[1]))
                        context.startActivity(Intent(context,MyExoPlayerActivity::class.java))

                }
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
