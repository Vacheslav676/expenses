package com.nominalista.expenses.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nominalista.expenses.data.model.Currency
import com.nominalista.expenses.data.model.Expense
import com.nominalista.expenses.data.model.Tag
import com.nominalista.expenses.util.extensions.toEpochMillis
import com.nominalista.expenses.util.extensions.toLocalDate
import com.nominalista.expenses.util.reactive.ReactiveDocumentSnapshotListener
import com.nominalista.expenses.util.reactive.ReactiveQuerySnapshotListener
import io.reactivex.Completable
import io.reactivex.Observable

class FirestoreDataSource(private val firestore: FirebaseFirestore) {

    // Expenses

    fun observeExpenses(): Observable<List<Expense>> {
        val expensesCollection = firestore.collection("expenses")
        val snapshotListener = ReactiveQuerySnapshotListener(expensesCollection)
        return Observable.create(snapshotListener)
            .map { query -> query.mapNotNull { mapDocumentToExpense(it) } }
    }

    fun observeExpense(id: String): Observable<Expense> {
        val expenseDocument = firestore.collection("expenses").document(id)
        val snapshotListener = ReactiveDocumentSnapshotListener(expenseDocument)
        return Observable.create(snapshotListener)
            .map { document -> mapDocumentToExpense(document) }
    }


    @Suppress("UNCHECKED_CAST")
    private fun mapDocumentToExpense(document: DocumentSnapshot): Expense? {
        val amount = document.getDouble("amount")?.toFloat()
            ?: return null

        val currency = document.getString("currency")?.let { Currency.fromCode(it) }
            ?: return null

        val title = document.getString("title")
            ?: return null

        val tags = (document.get("tags") as? List<Any>)
            ?.mapNotNull { it as? Map<Any, Any> }
            ?.mapNotNull { mapMapToTag(it) }
            ?: return null

        val date = document.getLong("date")?.toLocalDate()
            ?: return null

        val notes = document.getString("notes")
            ?: return null

        val timestamp = document.getTimestamp("timestamp")?.toDate()?.time

        return Expense(
            document.id,
            amount,
            currency,
            title,
            tags,
            date,
            notes,
            timestamp
        )
    }

    private fun mapMapToTag(map: Map<Any, Any>): Tag? {
        val id = map["id"] as? String ?: return null
        val name = map["name"] as? String ?: return null
        return Tag(id, name)
    }

    fun insertExpense(expense: Expense): Completable {
        val expensesReference = firestore.collection("expenses")

        val document = hashMapOf(
            "amount" to expense.amount,
            "currency" to expense.currency.code,
            "title" to expense.title,
            "tags" to expense.tags.map { mapOf("id" to it.id, "name" to it.name) },
            "date" to expense.date.toEpochMillis(),
            "notes" to expense.notes,
            "timestamp" to FieldValue.serverTimestamp()
        )

        return Completable.fromAction { expensesReference.add(document) }
    }

    fun updateExpense(expense: Expense): Completable {
        val expenseReference = firestore.collection("expenses").document(expense.id)

        val data = hashMapOf(
            "amount" to expense.amount,
            "currency" to expense.currency.code,
            "title" to expense.title,
            "tags" to expense.tags.map { mapOf("id" to it.id, "name" to it.name) },
            "date" to expense.date.toEpochMillis(),
            "notes" to expense.notes
        )

        return Completable.fromAction {
            expenseReference.update(data)
        }
    }

    fun deleteExpense(expense: Expense): Completable {
        val expenseReference = firestore.collection("expenses").document(expense.id)
        return Completable.fromAction { expenseReference.delete() }
    }

    // Tags

    fun observeTags(): Observable<List<Tag>> {
        val expensesCollection = firestore.collection("tags")
        val snapshotListener = ReactiveQuerySnapshotListener(expensesCollection)
        return Observable.create(snapshotListener)
            .map { query -> query.mapNotNull { mapDocumentToTag(it) } }
    }

    private fun mapDocumentToTag(document: DocumentSnapshot): Tag? {
        val name = document.getString("name") ?: return null
        return Tag(document.id, name)
    }

    fun insertTag(tag: Tag): Completable {
        val tagReference = firestore.collection("tags")

        val document = hashMapOf(
            "name" to tag.name
        )

        return Completable.fromAction { tagReference.add(document) }
    }

    fun deleteTag(tag: Tag): Completable {
        val tagReference = firestore.collection("tags").document(tag.id)
        return Completable.fromAction { tagReference.delete() }
    }

    companion object {
        private const val TAG = "FirestoreDataSource"
    }
}