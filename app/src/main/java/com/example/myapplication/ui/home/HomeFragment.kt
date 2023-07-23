package com.example.myapplication.ui.home

import androidx.compose.ui.graphics.Color

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Person
import com.example.myapplication.PersonDatabase
import com.example.myapplication.R
import java.util.Locale
import kotlin.math.roundToInt





@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PersonItem(
    person: Person,
    onSwipeToDelete: (Person) -> Unit,
    updateFunc: (Boolean) -> Unit,
    favorite: Boolean
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val size = 200.dp
    val anchors = mapOf(0f to 0, -size.value * LocalConfiguration.current.densityDpi / 160f to 1)

    Box(
        modifier = Modifier
            .fillMaxWidth()

            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Horizontal
            )

        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .offset {
                    IntOffset(
                        if (swipeableState.offset.value.isNaN()) 0 else swipeableState.offset.value.roundToInt(),
                        0
                    )
                }
                .padding(bottom = 16.dp, top = 16.dp)
                .height(25.dp)

            ) {
            Text(
                text = person.firstname,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Normal),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = person.name,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { updateFunc(!favorite) }) {
                Icon(
                    painterResource(id = if (favorite)  R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24 ),
                    contentDescription = if (favorite) "Remove from favorites" else "Add to favorites",
                    tint = Color(0xFFFFA41B)
                )
            }
        }

        // Delete Icon
        AnimatedVisibility(
            visible = swipeableState.offset.value < 0,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Red)
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .width(with(LocalDensity.current) { -swipeableState.offset.value.toDp() })
                    .height(25.dp)

            ) {
                Icon(
                    painterResource(id = R.drawable.delete_icon),
                    contentDescription = "Delete",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFB2B2B2))
                .align(Alignment.BottomStart)
        )
    }

    LaunchedEffect(swipeableState.currentValue) {
        if (swipeableState.currentValue == 1) {
            Log.d("TAG", "PersonItem: ${person.name}")
            onSwipeToDelete(person)
            swipeableState.animateTo(0)
        }
    }
}
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "Search",
    leadingIcon: ImageVector = Icons.Default.Search,
    onLeadingIconClick: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(Color.Transparent)
            .height(50.dp),
        textStyle = TextStyle(color = Color.Black),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color(0xFFEEEEEE),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.clickable(onClick = onLeadingIconClick)
            )
        },
        shape = RoundedCornerShape(25.dp),
        singleLine = true,
        placeholder = { Text(text = hint, color = Color.Gray) }
    )
}

@Composable
fun Header(
    onMenuClick: () -> Unit,
    Title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(

            text = Title,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .weight(0.8f)
                .wrapContentWidth(Alignment.Start)
                .wrapContentHeight(Alignment.CenterVertically)
        )
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .weight(0.2f)
                .wrapContentWidth(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color.Black,
                 modifier = Modifier
                     .alpha(0.65f)
                     .size(40.dp)

            )
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenHome(
    personListState: MutableState<List<Person>>?,
    personList: List<Person>,
    onExportAll: () -> Unit,
    onDeleteAll: () -> Unit,
    deletePersonInBackground: (Person) -> Unit,
    personDB: PersonDatabase,
    personInBackground: () -> Unit,
    personInBackgroundNotif: () -> Unit,
    totalState: MutableState<Int>
){

    Log.d("ScreenHome", "SET")

    Log.d("ScreenHome", "PERSON" + personListState?.value)
    val groupedPersons = personListState?.value?.groupBy { person ->
        val firstLetter = person.name.first().uppercaseChar()
        if (firstLetter in 'A'..'Z') firstLetter.toString() else "#"
    }?.toSortedMap(compareBy<String> { it == "#" }.thenBy { it })



    var searchText by remember { mutableStateOf("") }

    var showMenu by remember { mutableStateOf(false) }
    val menuItems = listOf(
        "Export All" to { onExportAll()
        },
        "Delete All" to {onDeleteAll()}
    )
    Column (modifier = Modifier.padding(10.dp)){


        Column {
            Header(onMenuClick = { showMenu = true },"Contacts")
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
                        personInBackgroundNotif()
                        personInBackground()
// Update personListState
                        personListState?.value = personListState?.value?.map { p ->
                            if (p.id == persons[person].id) p.copy(IsFavorite = newFavoriteValue) else p
                        } ?: emptyList()

                    },

                        favorite = persons[person].IsFavorite)}


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
