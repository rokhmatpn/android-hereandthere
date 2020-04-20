package com.supersamin.hereandthere

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowId
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.treecheckboxes_rows.view.*
import java.io.StringReader
import kotlin.coroutines.coroutineContext
import kotlin.reflect.jvm.internal.impl.util.Check


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var Categories: ArrayList<CatModel>
        lateinit var recyclerContext : RecyclerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonstring = """[
        {"id" : "1", "name" : "satu", "parent" : "0", "level" : "0" },
        {"id" : "2", "name" : "dua", "parent" : "1", "level" : "1" },
        {"id" : "3", "name" : "tiga", "parent" : "1", "level" : "1" },
        {"id" : "4", "name" : "empat", "parent" : "0", "level" : "0" },
        {"id" : "5", "name" : "lima", "parent" : "0", "level" : "0" },
        {"id" : "6", "name" : "enam", "parent" : "5", "level" : "1" },
        {"id" : "7", "name" : "tujuh", "parent" : "6", "level" : "2" },
        {"id" : "8", "name" : "delapan", "parent" : "7", "level" : "3" }
        {"id" : "9", "name" : "sembilan", "parent" : "7", "level" : "3" }
        ]"""

        val adapter = GroupAdapter<GroupieViewHolder>()
        Categories = ArrayList<CatModel>()

        val density = this.resources.displayMetrics.density.toInt()
        recyclerContext = this.categoryRecyclerView

        val klaxon = Klaxon()
        JsonReader(StringReader(jsonstring)).use { reader ->
            val result = arrayListOf<Category>()
            reader.beginArray {
                while (reader.hasNext()) {
                    val category = klaxon.parse<Category>(reader)
                    if (category != null) {
                        val catmodel = CatModel()
                        catmodel.isSelected = false
                        catmodel.Id = category.id.toInt()
                        catmodel.Name = category.name
                        catmodel.Level = category.level.toInt()
                        catmodel.ParentId = category.parent.toInt()

                        adapter.add(CatItem(density, catmodel))
                        Categories.add(catmodel)
                    }
                }
            }
        }

        categoryRecyclerView.adapter = adapter

        button.setOnClickListener {
            var selectedcategories: String = ""
            for (i in 0 until Categories.size) {
                val cat = Categories[i]
                if (cat.isSelected) {
                    Log.d(
                        "test",
                        "id=${cat.Id} name=${cat.isSelected}"
                    )
                    selectedcategories += "${cat.Id.toString()} ,"
                }
            }
            Toast.makeText(this, "Selected categoeries : $selectedcategories", Toast.LENGTH_LONG).show()

        }

    }
}

class CatItem(val density: Int, val category: CatModel): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val catcat = category
        catcat.position = position

        viewHolder.itemView.valueCheckBox.text = "${catcat.Name}"

        // set indent
        val checkbox = viewHolder.itemView.valueCheckBox
        var params = checkbox.layoutParams as ConstraintLayout.LayoutParams
        params.leftMargin = category.Level * 30 * density
        checkbox.layoutParams = params

        // to record the result
        viewHolder.itemView.tag = catcat.Id
        viewHolder.itemView.valueCheckBox.tag = catcat
        viewHolder.itemView.valueCheckBox.setOnClickListener() {
            val cat = it.valueCheckBox.tag as CatModel

            if (it.valueCheckBox.isChecked) {

                MainActivity.Categories[cat.position].isSelected = true

                // check uncheck parent
                var parentView: ViewGroup?
                parentView = MainActivity.recyclerContext.findViewWithTag<ViewGroup>(cat.ParentId)
                while (parentView != null) {
                    parentView.valueCheckBox.isChecked = it.valueCheckBox.isChecked

                    val parentTag = parentView.valueCheckBox.tag as CatModel
                    Log.d("test parent tag", "${parentTag.ParentId}")
                    parentView =
                        MainActivity.recyclerContext.findViewWithTag<ViewGroup>(parentTag.ParentId)
                }
            } else {

                // check if there is any childs
                val isChildChecked = checkChildsChecked(cat.Id)
                if (isChildChecked) {
                    Toast.makeText(it.context, "There are some childs that are still checked", Toast.LENGTH_LONG).show()
                    it.valueCheckBox.isChecked = true
                } else {
                    MainActivity.Categories[cat.position].isSelected = false
                }
            }
        }
    }

    fun checkChildsChecked(id: Int): Boolean {
        var isChildChecked: Boolean = false
        MainActivity.Categories.filter { it.ParentId == id }.forEach {
            isChildChecked = it.isSelected

            if (isChildChecked) {
                return isChildChecked
            } else {
                return checkChildsChecked(it.Id)
            }
        }
        return isChildChecked
    }

    override fun getLayout(): Int {
        return R.layout.treecheckboxes_rows
    }
}

class Category(val id: String, var name: String, val parent: String, val level: String)

class CatModel {
    var isSelected: Boolean = false
    var Id: Int = 0
    var Name: String = ""
    var Level: Int = 0
    var ParentId: Int = 0
    var position: Int = 0

    fun getIds(): Int {
        return Id
    }

    fun setIds(id: Int) {
        Id  = id
    }

    fun getSelecteds(): Boolean {
        return isSelected
    }

    fun setSelecteds(selected: Boolean) {
        isSelected = selected
    }
}
