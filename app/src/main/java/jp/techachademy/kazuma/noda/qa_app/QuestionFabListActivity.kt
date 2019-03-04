package jp.techachademy.kazuma.noda.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class QuestionFabListActivity : AppCompatActivity() {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionListAdapter

    var data = HashMap<String, String>()
    var fabList = arrayListOf<String>()

    private var categoryNumber = 4

    private val mEventListener = object: ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
            val fabSnap = dataSnapshot.value as String
            fabList.add(fabSnap)
            arrayArrange()

        }

        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            arrayArrange()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_fab_list)

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val mFabRef = mDatabaseReference.child(UserPATH).child(user!!.uid).child("fab")

        mListView = findViewById(R.id.listView)
        mAdapter = QuestionListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter

        mFabRef.addChildEventListener(mEventListener)

        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    private fun arrayArrange() {
        mQuestionArrayList.clear()

        for (genre in 0..categoryNumber) {
            val mQuestionListRef = mDatabaseReference.child("contents").child(genre.toString())

            mQuestionListRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val map = dataSnapshot.value as Map<String, String>
                        val snapShotKey = dataSnapshot.key

                        if (fabList.contains(snapShotKey)) {
                            val title = map["title"] ?: ""
                            val body = map["body"] ?: ""
                            val name = map["name"] ?: ""
                            val uid = map["uid"] ?: ""
                            val imageString = map["image"] ?: ""
                            val mGenre = map["genre"]!!.toInt()
                            val bytes =
                                if (imageString.isNotEmpty()) {
                                    Base64.decode(imageString, Base64.DEFAULT)
                                } else {
                                    byteArrayOf()
                                }

                            val answerArrayList = ArrayList<Answer>()
                            val answerMap = map["answers"] as Map<String, String>
                            if (answerMap != null) {
                                for (key in answerMap.keys) {
                                    val temp = answerMap[key] as Map<String, String>
                                    val answerBody = temp["body"] ?: ""
                                    val answerName = temp["name"] ?: ""
                                    val answerUid = temp["uid"] ?: ""
                                    val answer = Answer(answerBody, answerName, answerUid, key)
                                    answerArrayList.add(answer)
                                }
                            }

                            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                                mGenre, bytes, answerArrayList)
                            mQuestionArrayList.add(question)


                        }
                    }
                }
            })
        }
        mAdapter.notifyDataSetChanged()
    }
}
