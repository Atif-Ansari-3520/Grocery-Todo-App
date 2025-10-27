package com.example.todoapp

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodoListScreen(viewModel: TodoViewModel) {
    val todoList by viewModel.todoList.observeAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("All") }

    val filteredList = when (filter) {
        "Done" -> todoList.filter { it.isDone }
        else -> todoList
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var recentlyDeletedItem by remember { mutableStateOf<Todo?>(null) }
    var snackbarJob by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Grocery")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Grocery List",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 30.sp
            )

            Text(
                text = "Click button if you done your task",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 4.dp),
                color = Color.Gray,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(label = "All Task", selected = filter == "All") {
                    filter = "All"
                }
                FilterButton(label = "Done Task", selected = filter == "Done") {
                    filter = "Done"
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredList) { item ->
                    GroceryItem(
                        item = item,
                        onToggle = {
                            viewModel.toggleDone(item)
                            snackbarJob?.cancel()
                            snackbarJob = coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (item.isDone) "Task no done" else "Task done"
                                )
                            }
                        },
                        onDelete = {
                            recentlyDeletedItem = item
                            viewModel.deleteTodo(item)
                            snackbarJob?.cancel()
                            snackbarJob = coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Item deleted",
                                    actionLabel = "Undo"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    recentlyDeletedItem?.let {
                                        viewModel.addTodo(it.itemName, it.quantity)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AddGroceryDialog(
                onDismiss = { showDialog = false },
                onSubmit = { name, qty ->
                    viewModel.addTodo(name, qty)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray
    val textColor = if (selected) Color.White else Color.Black

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = background)
    ) {
        Text(text = label, color = textColor)
    }
}

@Composable
fun GroceryItem(item: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone) Color(0xFFB2DFDB) else MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("hh:mm a, dd/MM/yy", Locale.ENGLISH).format(item.createdAt),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(text = item.itemName, fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Qty: ${item.quantity}", fontSize = 16.sp, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = item.isDone,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AddGroceryDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var itemName by remember { mutableStateOf(TextFieldValue("")) }
    var quantity by remember { mutableStateOf(TextFieldValue("")) }

    val focusManager = LocalFocusManager.current
    val quantityFocusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val name = itemName.text.trim()
                val qty = quantity.text.trim()
                if (name.isNotEmpty() && qty.isNotEmpty()) {
                    onSubmit(name, qty)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Add Grocery Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { quantityFocusRequester.requestFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity / Weight") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(quantityFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
        }
    )
}
