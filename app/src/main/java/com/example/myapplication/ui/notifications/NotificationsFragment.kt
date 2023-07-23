
import androidx.compose.ui.graphics.Color


import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Person
import com.example.myapplication.PersonDatabase
import com.example.myapplication.ui.home.Header
import com.example.myapplication.ui.home.PersonItem
import com.example.myapplication.ui.home.SearchBar
import java.util.Locale
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenNotif(
    Clear: () -> Unit,
    personListState: MutableState<List<Person>>?,
    personList: List<Person>,
    onExportAll: () -> Unit,
    onDeleteAll: () -> Unit,
    deletePersonInBackground: (Person) -> Unit,
    personDB: PersonDatabase,
    personInBackground: () -> Unit,
    personInBackground1: () -> Unit,
    totalState: MutableState<Int>
){

    Log.d("ScreenNotif", "SETTTTTTTTTTTT")
    Log.d("ScreenNotif", "PERSON" + personListState?.value)


    val groupedPersons = personListState?.value?.groupBy { person ->
        val firstLetter = person.name.first().uppercaseChar()
        if (firstLetter in 'A'..'Z') firstLetter.toString() else "#"
    }?.toSortedMap(compareBy<String> { it == "#" }.thenBy { it })

    var searchText by remember { mutableStateOf("") }

    var showMenu by remember { mutableStateOf(false) }
    val menuItems = listOf(
        "Export only favorites" to { onExportAll()
            personInBackground()
            personInBackground1()
        },
        "Delete only favorites" to {onDeleteAll()
            personInBackground()
            personInBackground1()},
        "Clear favorites" to { Clear()
            personInBackground()
            personInBackground1()},
    )
    Column (modifier = Modifier.padding(10.dp)){


        Column {
            Header(onMenuClick = { showMenu = true },"Favorites")
            if (showMenu) {
                DropdownMenu(
                    // put the menu to the right
                    offset = DpOffset((-20).dp, 0.dp),
                    expanded = true,
                    onDismissRequest = { showMenu = false }
                ) {
                    menuItems.forEach { (title, onClick) ->
                        DropdownMenuItem(onClick = {
                            onClick()
                            showMenu = false
                        }) {
                            Text(text = title)
                        }
                    }
                }
            }
        }

        SearchBar(
            value = searchText,
            onValueChange = { //make a search on name and first
                searchText = it
                Log.d("search", searchText)
                if (personListState != null) {
                    personListState.value = personList.filter { person ->
                        person.name.lowercase(Locale.getDefault())
                            .contains(searchText.lowercase(Locale.getDefault())) ||
                                person.firstname.lowercase(Locale.getDefault())
                                    .contains(searchText.lowercase(Locale.getDefault()))
                    }
                }
                Log.d("search", personList.toString())
            },
            modifier = Modifier
                .fillMaxWidth()
        )


        LazyColumn {
            groupedPersons?.forEach { (initial, persons) ->
                stickyHeader {
                    Surface(color = Color.White, modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)) {
                        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = initial,
                                style = TextStyle(
                                    color = Color.Gray,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFB2B2B2))
                    )
                }

                items(persons.size) { person ->
                    Log.d("HomeFragment", "Person: ${persons[person]}")

                    PersonItem(person = persons[person], onSwipeToDelete = { personToDelete ->
                        // Perform delete action here
                        deletePersonInBackground(personToDelete)
                        Log.d("HomeFragment", "deletePersonInBackground: $personToDelete")
                    }, updateFunc = { newFavoriteValue ->
                        persons[person].id?.let { personDB.personDAO().toggleFavorite(it) }
                        // Update persons list
                        val updatedPersons = persons.toMutableList()
                        updatedPersons[person] = updatedPersons[person].copy(IsFavorite = newFavoriteValue)
                        groupedPersons?.set(initial, updatedPersons)

// Update personListState
                        personListState?.value = personListState?.value?.map { p ->
                            if (p.id == persons[person].id) p.copy(IsFavorite = newFavoriteValue) else p
                        } ?: emptyList()

                    },
                        favorite = persons[person].IsFavorite)
                    personInBackground()
                    personInBackground1()
                }


            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)

                    ,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (totalState.value > 0) {
                        Text(
                            text = "${totalState.value} contacts",
                            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
                        )
                    } else if (totalState.value == 0) {
                        Text(
                            text = "No contacts",
                            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
                        )
                    } else if (totalState.value == 1) {
                        Text(
                            text = "${totalState.value} contact",
                            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(150.dp)) }
        }
    }
}