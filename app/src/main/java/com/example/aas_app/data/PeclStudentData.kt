package com.example.aas_app.data

import com.example.aas_app.data.entity.PeclStudentEntity

object PeclStudentData {
    val studentData = listOf(
        PeclStudentEntity(firstName = "Example", lastName = "Student1", grade = "Cpl", pin = 101, fullName = "Student1, Example"),
        PeclStudentEntity(firstName = "Example", lastName = "Student2", grade = "Sgt", pin = 102, fullName = "Student2, Example"),
        PeclStudentEntity(firstName = "Example", lastName = "Student3", grade = "LCpl", pin = 103, fullName = "Student3, Example"),
        PeclStudentEntity(firstName = "Example", lastName = "Student4", grade = "Cpl", pin = 104, fullName = "Student4, Example"),
        PeclStudentEntity(firstName = "Example", lastName = "Student5", grade = "Cpl", pin = 105, fullName = "Student5, Example"),
        PeclStudentEntity(firstName = "Example", lastName = "Student6", grade = "Pvt", pin = 106, fullName = "Student6, Example")
    )
}