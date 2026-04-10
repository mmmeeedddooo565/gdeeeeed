@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.mesm.clinic

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.mesm.clinic.data.AppDatabase
import com.mesm.clinic.data.AuthStore
import com.mesm.clinic.data.CaseImageEntity
import com.mesm.clinic.data.ClinicRepository
import com.mesm.clinic.ui.ClinicViewModel
import com.mesm.clinic.utils.ImageStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = ClinicRepository(AppDatabase.get(this).dao())
        val auth = AuthStore(this)
        setContent {
            MaterialTheme {
                AppRoot(repo, auth, this)
            }
        }
    }
}

@Composable
fun AppRoot(repo: ClinicRepository, auth: AuthStore, activity: ComponentActivity) {
    val vm: ClinicViewModel = viewModel(factory = ClinicViewModel.Factory(repo))
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "login") {
        composable("login") {
            LoginScreen(auth, activity) {
                nav.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("home") {
            HomeScreen(
                onSearch = { nav.navigate("search") },
                onAdd = { vm.newCase(); nav.navigate("details") }
            )
        }
        composable("search") {
            SearchScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onOpen = { id ->
                    vm.loadCase(id)
                    nav.navigate("view/$id")
                },
                onAdd = {
                    vm.newCase()
                    nav.navigate("details")
                }
            )
        }
        composable("details") {
            DetailsScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onNext = { vm.save { nav.navigate("images/$it") } }
            )
        }
        composable(
            "images/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { back ->
            val caseId = back.arguments?.getLong("id") ?: 0L
            ImagesScreen(
                vm = vm,
                caseId = caseId,
                onBack = { nav.popBackStack() },
                onDone = { nav.navigate("view/$caseId") }
            )
        }
        composable(
            "view/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            ViewScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate("details") },
                onDeleted = {
                    nav.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(auth: AuthStore, activity: ComponentActivity, onLogin: () -> Unit) {
    var username by rememberSaveable { mutableStateOf(auth.username()) }
    var password by rememberSaveable { mutableStateOf("") }
    var showChange by remember { mutableStateOf(false) }
    var newUser by rememberSaveable { mutableStateOf("") }
    var newPass by rememberSaveable { mutableStateOf("") }
    val bioOk = false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تسجيل الدخول",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("Professional clinic file app", color = Color.Gray)

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (username == auth.username() && password == auth.password()) {
                            if (!auth.changed()) showChange = true else onLogin()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("دخول")
                }

                OutlinedButton(
                    onClick = { },
                    enabled = bioOk,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("دخول بالبصمة")
                }

                Text(
                    text = "M. Esm",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showChange) {
        val scope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = {},
            title = { Text("تغيير بيانات الدخول") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newUser,
                        onValueChange = { newUser = it },
                        label = { Text("اسم المستخدم الجديد") }
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("كلمة المرور الجديدة") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUser.isNotBlank() && newPass.isNotBlank()) {
                            scope.launch {
                                auth.saveCredentials(newUser, newPass)
                                showChange = false
                                onLogin()
                            }
                        }
                    }
                ) {
                    Text("حفظ البيانات")
                }
            },
            dismissButton = {}
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(onSearch: () -> Unit, onAdd: () -> Unit) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("M. Esm Clinic") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActionCard("بحث", "الوصول لملف مريض موجود", Icons.Default.Search, onSearch)
            ActionCard("إضافة", "إضافة ملف جديد خطوة بخطوة", Icons.Default.Add, onAdd)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: ClinicViewModel, onBack: () -> Unit, onOpen: (Long) -> Unit, onAdd: () -> Unit) {
    val cases by vm.cases.collectAsState()
    var q by rememberSaveable { mutableStateOf("") }

    val filtered = remember(cases, q) {
        cases.filter {
            q.isBlank() ||
                it.patientName.contains(q, ignoreCase = true) ||
                it.caseNumber.contains(q, ignoreCase = true) ||
                it.diagnosis.contains(q, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("بحث") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = { Text("ابحث بالاسم أو الرقم أو التشخيص") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered) { item ->
                    Card(onClick = { onOpen(item.id) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.patientName, fontWeight = FontWeight.Bold)
                                Text("#${item.caseNumber}")
                            }
                            Text(item.diagnosis, color = Color.Gray)
                            Text("العمر: ${item.age}", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(vm: ClinicViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    val form by vm.form.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("بيانات الملف") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = form.caseNumber,
                    onValueChange = { value -> vm.update { f -> f.copy(caseNumber = value) } },
                    label = { Text("رقم الحالة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.patientName,
                    onValueChange = { value -> vm.update { f -> f.copy(patientName = value) } },
                    label = { Text("اسم المريض") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.age,
                    onValueChange = { value -> vm.update { f -> f.copy(age = value) } },
                    label = { Text("العمر") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.diagnosis,
                    onValueChange = { value -> vm.update { f -> f.copy(diagnosis = value) } },
                    label = { Text("التشخيص") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.visitDate,
                    onValueChange = { value -> vm.update { f -> f.copy(visitDate = value) } },
                    label = { Text("التاريخ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.noteHint,
                    onValueChange = { value -> vm.update { f -> f.copy(noteHint = value) } },
                    label = { Text("معلومة إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.notes,
                    onValueChange = { value -> vm.update { f -> f.copy(notes = value) } },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text("التالي")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImagesScreen(vm: ClinicViewModel, caseId: Long, onBack: () -> Unit, onDone: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(caseId) { vm.loadCase(caseId) }
    val form by vm.form.collectAsState()

    val caseLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { vm.addImage(ImageStorage.copyToPrivateStorage(context, it), "case") }
    }

    val rxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { vm.addImage(ImageStorage.copyToPrivateStorage(context, it), "rx") }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الصور") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = { caseLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة صور الحالة")
                }
            }
            item {
                Button(
                    onClick = { rxLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة صور الروشتة")
                }
            }
            item { Text("صور الحالة", fontWeight = FontWeight.Bold) }
            item { ImageGrid(form.caseImages) { vm.deleteImage(it) } }
            item { Text("صور الروشتة", fontWeight = FontWeight.Bold) }
            item { ImageGrid(form.rxImages) { vm.deleteImage(it) } }
            item {
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text("إنهاء")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ImageGrid(images: List<CaseImageEntity>, onDelete: (CaseImageEntity) -> Unit) {
    if (images.isEmpty()) {
        Text("لا توجد صور", color = Color.Gray)
        return
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.forEach { img ->
            Card(modifier = Modifier.size(110.dp)) {
                Box {
                    AsyncImage(
                        model = Uri.parse(img.uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { onDelete(img) },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScreen(vm: ClinicViewModel, onBack: () -> Unit, onEdit: () -> Unit, onDeleted: () -> Unit) {
    val item by vm.viewCase.collectAsState()
    val c = item ?: return

    var caseIndex by remember { mutableIntStateOf(0) }
    var rxIndex by remember { mutableIntStateOf(0) }
    val caseImages = c.images.filter { it.kind == "case" }
    val rxImages = c.images.filter { it.kind == "rx" }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ملف المريض") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                    IconButton(onClick = { vm.deleteCurrent(onDeleted) }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                c.case.patientName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("#${c.case.caseNumber}")
                        }
                        Text("العمر: ${c.case.age}")
                        Text("التشخيص: ${c.case.diagnosis}")
                        Text("التاريخ: ${c.case.visitDate.ifBlank { "—" }}")
                        Text("ملاحظات: ${c.case.notes.ifBlank { "لا توجد" }}")
                    }
                }
            }
            item { Text("صور الحالة", fontWeight = FontWeight.Bold) }
            item { SwipeCarousel(caseImages, caseIndex, { caseIndex = it }) }
            item { Text("صور الروشتة", fontWeight = FontWeight.Bold) }
            item { SwipeCarousel(rxImages, rxIndex, { rxIndex = it }) }
        }
    }
}

@Composable
fun SwipeCarousel(images: List<CaseImageEntity>, index: Int, onIndexChange: (Int) -> Unit) {
    if (images.isEmpty()) {
        Card {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد صور")
            }
        }
        return
    }

    Card {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .pointerInput(images, index) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -25) onIndexChange((index + 1) % images.size)
                        if (dragAmount > 25) onIndexChange((index - 1 + images.size) % images.size)
                    }
                }
        ) {
            AsyncImage(
                model = Uri.parse(images[index].uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "${index + 1} / ${images.size}",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp)
                    .background(
                        Color.Black.copy(alpha = 0.35f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White
            )
        }
    }
}
