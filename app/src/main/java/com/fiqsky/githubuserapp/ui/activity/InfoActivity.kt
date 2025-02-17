package com.fiqsky.githubuserapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.fiqsky.githubuserapp.R
import com.fiqsky.githubuserapp.api.ApiClient
import com.fiqsky.githubuserapp.ui.adapter.SectionAdapter
import com.fiqsky.githubuserapp.ui.fragment.FollowingFragment
import com.fiqsky.githubuserapp.utils.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.desc_user.*
import kotlinx.android.synthetic.main.info_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class InfoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER = "user"
    }

    private lateinit var adapter: SectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val user = intent.getParcelableExtra<User>(EXTRA_USER)
        val userName = user?.userName ?: ""
        getDetail(userName)

        title = userName

        adapter = SectionAdapter(supportFragmentManager)
        view_pager.adapter = adapter
        tabs.setupWithViewPager(view_pager)
    }

    private fun getFollowers(userName: String, title: String) {
        val call = ApiClient.service.getFollowers(userName)
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(
                call: Call<List<User>>, response: Response<List<User>>
            ) {
                Log.d("message", "onResponse: " + response.body())
                if (response.isSuccessful) {
                    val list = ArrayList<User>(response.body().orEmpty())
                    adapter.addFragment(FollowingFragment.newInstance(list), title)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.d("message", "onFailure: " + t.message)
            }

        })
    }

    private fun getFollowing(userName: String, title: String) {
        val call = ApiClient.service.getFollowing(userName)
        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                Log.d("message", "onResponse: " + response.body())
                val list = ArrayList<User>(response.body().orEmpty())
                adapter.addFragment(FollowingFragment.newInstance(list), title)
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.d("message", "onFailure: " + t.message)
            }

        })

    }

    private fun getDetail(userName: String) {
        val call = ApiClient.service.getDetail(userName)
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.d("message", "onResponse: " + response.body())
                if (response.isSuccessful) {
                    val user = response.body()
                    initDetailUser(user)
                    getFollowing(userName, "${user?.totalFollowing} Following")
                    getFollowers(userName, "${user?.totalFollowers} Followers")
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("message", "onFailure: " + t.message)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initDetailUser(user: User?) {
        txt_repo.text = user?.publicRepos.toString()
        txt_followers.text = user?.totalFollowers.toString()
        txt_followings.text = user?.totalFollowing.toString()

        if (user?.name != null) {
            txt_name.text = user.name
        } else {
            txt_name.text = "Unknown"
        }

        if (user?.location != null) {
            txt_location.text = user.location
        } else {
            txt_location.text = "-"
        }

        if (user?.company != null) {
            txt_work.text = user.company
        } else {
            txt_work.text = "-"
        }

        if (user?.blog != null) {
            val url = user.blog
            txt_link.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
            txt_link.text = url
        } else {
            txt_link.text = "-"
        }

        Picasso.get()
            .load(user?.avatarUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.color.design_default_color_error)
            .into(img_avatar)
    }
}