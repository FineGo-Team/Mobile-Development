package com.dicoding.finego.features.profile

data class UserInputRequest(
    val profile: Profile,
    val expenses: Expenses,
    val income: Income
)

data class Profile(
    val name: String,
    val email: String,
    val birthdate: String,  // Format: "YYYY-MM-DD"
    val province: String,
)

data class Expenses(
    val food_expenses: Int,
    val transportation_expenses: Int,
    val housing_cost: Int,
    val electricity_bill: Int,
    val water_bill: Int,
    val internet_cost: Int,
    val debt: Int
)

data class Income(
    val total_income: Int,
    val savings: Int
)