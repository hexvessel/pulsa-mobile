package com.example.pulsa.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pulsa.R
import com.example.pulsa.adapters.GenericRecyclerAdapter
import com.example.pulsa.databinding.ActivityMainBinding
import com.example.pulsa.networking.NetworkManager
import com.example.pulsa.objects.Post
import com.google.gson.reflect.TypeToken

class MainActivity : BaseLayoutActivity(), ActivityRing<Post> {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: GenericRecyclerAdapter<Post>
    private lateinit var posts: MutableList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runOnUiThread {
            NetworkManager().get(
                this,
                hashMapOf(
                    "type" to object : TypeToken<List<Post>>() {},
                    "url" to ""
                )
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            allsubsbtn.setOnClickListener {
                startActivity(Intent(this@MainActivity, SubIndexActivity::class.java))
            }
            onBackPressedDispatcher.addCallback(
                this@MainActivity,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        recyclerView.smoothScrollToPosition(0)
                    }
                }
            )
        }
    }

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.extras
                val pos = data?.getInt("pos")!!

                if (data.getBoolean("nextPost", false)) {
                    val (post, position) = next(posts, pos)

                    dispatch(post, position, ::adapterOnClick)
                } else if (data.getBoolean("prevPost", false)) {
                    val (post, position) = prev(posts, pos)

                    dispatch(post, position, ::adapterOnClick)
                } else {
                    val post: Post = result.data?.extras?.getParcelable("postWithReply")!!

                    post?.let { it ->
                        adapter.updateItem(post, pos)
                        posts[pos] = post
                    }

                }
            }
        }

    override fun resolveGet(content: Any) {
        posts = content as MutableList<Post>
        adapter = GenericRecyclerAdapter(posts, ::adapterOnClick, R.layout.post_item)
        voteOnClickSetup()
        binding.recyclerView.adapter = adapter
    }

    private fun adapterOnClick(post: Post, position: Int) {
        val intent = Intent(this, PostActivity::class.java)
        intent.putExtra("post", post)
        intent.putExtra("pos", position)
        resultLauncher.launch(intent)
    }

    public fun failure() {
        // TODO: Display failed to load posts xml
        // TODO: Rename function
        println("Failed to load posts")
    }

    override fun onResume() {
        super.onResume()

        if (this::adapter.isInitialized) adapter.notifyDataSetChanged()
        super.setupUserMenu()
    }

    override fun dispatch(content: Post, position: Int, launcher: (Post, Int) -> Unit) {
        adapterOnClick(content, position)
    }

    private fun voteOnClickSetup() {
        adapter.upvoteOnClick { id, pos ->
            runOnUiThread {
                NetworkManager().post(
                    this, hashMapOf(
                        "type" to object : TypeToken<Post>() {},
                        "url" to "p/${id}/upvote",
                        "vote" to ""
                    )
                )
            }
            intent.putExtra("pos", pos)
        }

        adapter.downvoteOnClick { id, pos ->
            runOnUiThread {
                NetworkManager().post(
                    this, hashMapOf(
                        "type" to object : TypeToken<Post>() {},
                        "url" to "p/${id}/downvote",
                        "vote" to ""
                    )
                )
            }
            intent.putExtra("pos", pos)
        }
    }

    override fun resolvePost(content: Any) {
        val votedPost = content as Post
        val position = intent.getIntExtra("pos", -1)!!
        adapter.updateItem(votedPost, position)
    }

}
