package jp.techachademy.kazuma.noda.qa_app

import android.content.Context
import android.hardware.input.InputManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*

class AnswerSendActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    private lateinit var mQuestion: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_send)

        val extra = intent.extras
        mQuestion = extra.get("question") as Question

        sendButton.setOnClickListener(this)
    }

    override fun onComplete(databaseError: DatabaseError?, p1: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "failed to submit", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onClick(v: View) {
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val mDatabaseReference = FirebaseDatabase.getInstance().reference
        val mData = mDatabaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.uid).child(AnswersPATH)

        val data = hashMapOf<String, String>()

        data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")

        data["name"] = name

        val body = answerEditText.text.toString()

        if (body.isEmpty()) {
            Snackbar.make(v, "Please fill in the answer", Snackbar.LENGTH_LONG).show()
            return
        }

        data["body"] = body

        progressBar.visibility = View.VISIBLE
        mData.push().setValue(data, this)
    }



}
