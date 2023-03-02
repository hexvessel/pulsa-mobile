package com.example.pulsa

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pulsa.databinding.ActivityNewReplyBinding

class NewReplyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewReplyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewReplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postbutton.setOnClickListener {
            val text = binding.newreplytext.text.toString()
            val user = User(
                1,
                "Anonymous",
                null,
                "Anonymous",
                "https://res.cloudinary.com/dc6h0nrwk/image/upload/v1668893599/a6zqfrxfflxw5gtspwjr.png"
            )

            val intent = Intent(this, PostActivity::class.java)
            val reply = Reply(
                35,
                Content(
                    36,
                    text,
                    "test",
                    "test",
                    "recording",
                    null,
                    null
                ),
                user,
                null,
                null,
                null,
                null,
                null,
                null
            )

            intent.putExtra("reply", reply)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}