package com.example.todoapp

import androidx.lifecycle.*
import kotlinx.coroutines.launch




class TodoViewModel(private val dao: TodoDao) : ViewModel() {
    val todoList: LiveData<List<Todo>> = dao.getAllTodos()

    fun addTodo(itemName: String, quantity: String) {
        if (itemName.isNotBlank() && quantity.isNotBlank()) {
            val todo = Todo(itemName = itemName, quantity = quantity)
            viewModelScope.launch { dao.insert(todo) }
        }
    }

    fun toggleDone(todo: Todo) {
        val updated = todo.copy(isDone = !todo.isDone)
        viewModelScope.launch { dao.update(updated) }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch { dao.delete(todo) }
    }
}



class TodoViewModelFactory(private val dao: TodoDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodoViewModel(dao) as T
    }
}
