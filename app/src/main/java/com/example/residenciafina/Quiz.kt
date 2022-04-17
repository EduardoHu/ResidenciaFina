package com.example.residenciafina

 class Quiz {
    var quizName:String=""
     lateinit var questions:ArrayList<Question>
    fun addQuestion(question: Question){
        if(null==question){
            questions = ArrayList()
        }
       questions.add(question)
    }
}