package jp.techachademy.kazuma.noda.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)


        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val mFabRef = databaseReference.child(UserPATH).child(user!!.uid).child("fab")
        val data = HashMap<String, String>()

        var flag = false
        mFabRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val map = dataSnapshot.value as Map<String, String>
                    Log.d("kotlintest", dataSnapshot.value.toString())
                    if (map.containsValue(mQuestion.questionUid)) {

                        imageButton.setImageResource(R.drawable.ic_fab_clicked)
                        flag = true
                    } else {
                        imageButton.setImageResource(R.drawable.ic_fab_border)
                        flag = false
                    }
                }
            }
        })

        imageButton.setOnClickListener {
            if (flag) {
                imageButton.setImageResource(R.drawable.ic_fab_border)
                mFabRef.orderByValue().equalTo(mQuestion.questionUid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.ref.child(mQuestion.questionUid).removeValue()
                    }
                })
                flag = false
            } else {
                imageButton.setImageResource(R.drawable.ic_fab_clicked)
                val mFabData = HashMap<String, Any>()
                mFabData[mQuestion.questionUid] = mQuestion.questionUid
                mFabRef.updateChildren(mFabData)
                flag = true
            }
        }


        mAnswerRef = databaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
}
