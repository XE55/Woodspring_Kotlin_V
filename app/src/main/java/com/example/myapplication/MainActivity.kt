package com.example.myapplication

import ScreenNotif
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.ui.home.ScreenHome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    lateinit var totalStateHome: MutableState<Int>
    lateinit var personListStateHome: MutableState<List<Person>>
    lateinit var personListHome: List<Person>
    var totalHome = 0
    lateinit var totalStateNotif: MutableState<Int>
    lateinit var personListStateNotif: MutableState<List<Person>>
    lateinit var personListNotif: List<Person>
    var totalNotif = 0
    lateinit var personDB: PersonDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //DB implementation
        val callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }
        }
        personDB = Room.databaseBuilder(applicationContext, PersonDatabase::class.java, "personDB")
            .allowMainThreadQueries()
            .addCallback(callback)
            .build()
       /* fabBtn = findViewById(R.id.fab)
        fabBtn?.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/comma-separated-values"
            startActivityForResult(intent, PICK_CSV_FILE)
        })*/
        //set bottom bar using jetpack

        personListHome = personDB.personDAO().allPerson
        personListNotif = personDB.personDAO().allFavoritePerson
        totalNotif = personDB.personDAO().totalFavorite
        totalHome = personDB.personDAO().total


        setContent {
            val items = listOf(
                Screen.Home,
                Screen.Notifications
            )
            val navController = rememberNavController()
            BottomBarWithFab(navController = navController,items = items,personListStateHome,personListHome,{ExportAll()},{DeleteAll()},{deletePersonInBackground(it)},personDB,{personInBackground()},totalStateHome,
                {Clear()},personListStateNotif,personListNotif,{ExportNotif()},{DeleteNotif()},{personInBackgroundNotif()},totalStateNotif)
        }
        totalStateHome = mutableStateOf(totalHome)
        totalStateNotif = mutableStateOf(totalNotif)
        personListStateHome = mutableStateOf(personListHome)
        personListStateNotif = mutableStateOf(personListNotif)

    }

    fun Clear(){

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                personDB!!.personDAO().unFavoriteAll()
                personInBackgroundNotif()
                personInBackground()
            }
            Toast.makeText(this@MainActivity, "All Favorites cleared", Toast.LENGTH_LONG).show()
        }
    }



    private fun ExportNotif (){
        // Handle Option 1 click


        // Create a FileWriter for the output file
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var outputFile = File(downloadsFolder, "Contacts.txt")
            var count = 1
            while (outputFile.exists()) {
                // Create a new file with a different name
                outputFile = File(downloadsFolder, "Contacts_Favorites($count).txt")
                count++
            }
            val fileWriter = FileWriter(outputFile)
            fileWriter.write("firstname,lastname\n")
            if (personListNotif != null) {
                for (person in personListNotif) {
                    fileWriter.write(
                        """
                ${person?.firstname},${person?.name}
                
                """.trimIndent()
                    )
                    Log.d(
                        "TAG", """
                 onMenuItemClick: ${person?.firstname},${person?.name}
                 
                 """.trimIndent()
                    )
                }}
            Log.d("TAG", "onMenuItemClick: " + outputFile.absolutePath)
            fileWriter.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun DeleteNotif(){
        // Handle Option 2 click and delete all
        Log.d("TAG", "onMenuItemClick: Delete All")
        //make in a corountine thread

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                personDB!!.personDAO().deleteAllFavorite()
                personInBackgroundNotif()
                personInBackground()
            }
            Toast.makeText(this@MainActivity, "All Favorites deleted", Toast.LENGTH_LONG).show()
        }

    }

    fun personInBackgroundNotif(){
        Log.d("TAG", "personInBackground: ")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                personListStateNotif.value = personDB.personDAO().allFavoritePerson
                totalStateNotif.value = personDB.personDAO().totalFavorite
            }
        }

    }
    fun deletePersonInBackground(person: Person?) {

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                personDB!!.personDAO().delete(person)
                personInBackground()
                personInBackgroundNotif()
            }
            Toast.makeText(this@MainActivity, "Person deleted", Toast.LENGTH_LONG).show()
        }

    }

    private fun ExportAll (){
        // Handle Option 1 click


        // Create a FileWriter for the output file
        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var outputFile = File(downloadsFolder, "Contacts.txt")
            var count = 1
            while (outputFile.exists()) {
                // Create a new file with a different name
                outputFile = File(downloadsFolder, "Contacts($count).txt")
                count++
            }
            val fileWriter = FileWriter(outputFile)
            fileWriter.write("firstname,lastname\n")
            if (personListHome != null) {
                for (person in personListHome) {
                    fileWriter.write(
                        """
                ${person?.firstname},${person?.name}
                
                """.trimIndent()
                    )
                    Log.d(
                        "TAG", """
                 onMenuItemClick: ${person?.firstname},${person?.name}
                 
                 """.trimIndent()
                    )
                }}
            Log.d("TAG", "onMenuItemClick: " + outputFile.absolutePath)
            fileWriter.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    //////////////////
    fun DeleteAll(){
        // Handle Option 2 click and delete all
        Log.d("TAG", "onMenuItemClick: Delete All")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                personDB!!.personDAO().deleteAll()
                personInBackground()
            }
            Toast.makeText(this@MainActivity, "All Person deleted", Toast.LENGTH_LONG).show()
        }

    }
    fun personInBackground(){
        Log.d("TAG", "personInBackground: ")

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                personListStateHome.value = personDB.personDAO().allPerson
                totalStateHome.value = personDB.personDAO().total
            }
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK) {
            val uri = data!!.data
            try {
                val inputStream = contentResolver.openInputStream(uri!!)
                val reader: Reader = InputStreamReader(inputStream)
                val parser = CSVParser(reader, CSVFormat.DEFAULT.withHeader())
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute {
                    val count_before = personDB!!.personDAO().total
                    for (record: CSVRecord in parser) {
                        val firstName = record["firstname"]
                        val lastName = record["lastname"]

                        // Check if the person already exists in the database
                        if (!personDB!!.personExists(firstName, lastName)) {
                            val person = Person(name = lastName, firstname = firstName, IsFavorite = false)
                            personDB!!.personDAO().insert(person)
                            //get total number of persons added
                        }
                    }
                    personInBackground()
                    val count_after = personDB!!.personDAO().total
                    val count_added = count_after - count_before
                    runOnUiThread(Runnable {
                        if (count_added == 0) {
                            Toast.makeText(this@MainActivity, "No person added", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            if (count_added == 1) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "$count_added person added",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "$count_added persons added",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()

            }
        }
    }


    fun addPersonInBackground(person: Person?) {
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(mainLooper)
        executorService.execute {
            personDB!!.personDAO().insert(person)
            handler.post {
                Toast.makeText(this@MainActivity, "Person added", Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        const val PICK_CSV_FILE = 1
        private const val TAG = "MainActivity"
    }


}


@Composable
fun BottomBarWithFab(
    navController: NavController,
    items: List<Screen>,
    PstateHome: MutableState<List<Person>>?,PsListHome:List<Person>  ,   onExportAll: () -> Unit, onDeleteAll: () -> Unit,deletePersonInBackground:(Person) -> Unit,personDB: PersonDatabase, personInBackground: () -> Unit,totalState: MutableState<Int>,
    Clear:() -> Unit,PstateNotif: MutableState<List<Person>>?,PsListNotif:List<Person>  ,   onExportNotif: () -> Unit, onDeleteNotif: () -> Unit, personInBackgroundNotif: () -> Unit,totalStateNotif: MutableState<Int>

) {
    val currentRoute = currentRoute(navController)
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val persons = readCsv(context, uri)
                insertPersons(personDB, persons)
                personInBackground()
            }
        }
    )
    Scaffold(
        bottomBar = {
            BottomAppBar(
                cutoutShape = CircleShape,
                backgroundColor = Color.White,
            ) {
                BottomNavigation(backgroundColor = Color.White) {
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(screen.icon, contentDescription = null, tint = Color(0xFF525FE1)) },
                            label = { Text(stringResource(screen.resourceId), color = Color(0xFF525FE1)   ) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    launcher.launch("text/comma-separated-values")
                },
                backgroundColor = Color(0xFFF86F03)


            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) { innerPadding ->
        NavHost(navController as NavHostController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { ScreenHome(
                PstateHome,
                PsListHome  ,
                onExportAll,
                onDeleteAll,
                deletePersonInBackground ,
                personDB,
                personInBackground ,
                personInBackgroundNotif,
                totalState
            ) }
            composable(Screen.Notifications.route) { ScreenNotif(
                Clear,
                PstateNotif  ,
                PsListNotif ,
                onExportNotif,
                onDeleteNotif,
                deletePersonInBackground ,
                personDB  ,
                personInBackgroundNotif,
                personInBackground,
                totalStateNotif
            ) }
        }
    }
}
fun readCsv(context: Context, uri: Uri): List<Person> {
    val persons = mutableListOf<Person>()
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val reader = InputStreamReader(inputStream)
        val parser = CSVParser(reader, CSVFormat.DEFAULT.withHeader())
        for (record in parser) {
            val firstName = record["firstname"]
            val lastName = record["lastname"]
            persons.add(Person(name = lastName, firstname = firstName, IsFavorite = false))
        }
    }
    return persons
}

fun insertPersons(personDB: PersonDatabase, persons: List<Person>) {
    for (person in persons) {
        if (!personDB.personExists(person.firstname, person.name)) {
            personDB.personDAO().insert(person)
        }
    }
}
enum class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    Home("home", R.string.title_home, Icons.Filled.Person),
    Notifications("notifications", R.string.title_notifications, Icons.Filled.Favorite)
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
