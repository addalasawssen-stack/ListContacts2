package com.example.listcontacts2

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.listcontacts2.model.Contact
import com.example.listcontacts2.ui.components.ContactAvatar
import com.example.listcontacts2.ui.theme.ListContacts2Theme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CONTACT = "extra_contact"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialContact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CONTACT, Contact::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONTACT)
        }

        setContent {
            ListContacts2Theme {
                DetailScreen(
                    initialContact = initialContact,
                    onSave = { updatedContact ->
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_CONTACT, updatedContact)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    initialContact: Contact?,
    onSave: (Contact) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    val contactId = remember { initialContact?.id ?: UUID.randomUUID().toString() }
    var nom by remember { mutableStateOf(initialContact?.nom ?: "") }
    var prenom by remember { mutableStateOf(initialContact?.prenom ?: "") }
    var telephone by remember { mutableStateOf(initialContact?.telephone ?: "") }
    var email by remember { mutableStateOf(initialContact?.email ?: "") }
    var photoUri by remember { mutableStateOf(initialContact?.photoUri) }

    var nomError by remember { mutableStateOf(false) }
    var prenomError by remember { mutableStateOf(false) }
    var telephoneError by remember { mutableStateOf(false) }

    // Camera temp file URI state
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist URI permission access if applicable
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not a persistable URI
            }
            photoUri = uri.toString()
            Toast.makeText(context, "Photo mise à jour depuis la galerie", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Sélection de photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for Camera (TakePicture)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            photoUri = tempCameraUri.toString()
            Toast.makeText(context, "Photo prise avec succès", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Prise de photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera(context) { uri ->
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
    }

    val isEditing = initialContact != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Détails du contact" else "Nouveau contact",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Contact Photo Avatar Header
                val currentInitials = remember(prenom, nom) {
                    val p = prenom.take(1).uppercase()
                    val n = nom.take(1).uppercase()
                    if (p.isNotEmpty() || n.isNotEmpty()) "$p$n" else "?"
                }

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    ContactAvatar(
                        photoUri = photoUri,
                        initials = currentInitials,
                        size = 120.dp
                    )
                }

                // Buttons to pick/take photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galerie")
                    }

                    OutlinedButton(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                launchCamera(context) { uri ->
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Caméra")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Editable Fields
                OutlinedTextField(
                    value = nom,
                    onValueChange = {
                        nom = it
                        nomError = false
                    },
                    label = { Text("Nom *") },
                    isError = nomError,
                    supportingText = { if (nomError) Text("Le nom est obligatoire") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = prenom,
                    onValueChange = {
                        prenom = it
                        prenomError = false
                    },
                    label = { Text("Prénom *") },
                    isError = prenomError,
                    supportingText = { if (prenomError) Text("Le prénom est obligatoire") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telephone,
                    onValueChange = {
                        telephone = it
                        telephoneError = false
                    },
                    label = { Text("Numéro de téléphone *") },
                    isError = telephoneError,
                    supportingText = { if (telephoneError) Text("Le numéro de téléphone est obligatoire") },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Adresse e-mail") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Implicit Intents: Appeler & Envoyer E-mail)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Appeler Button
                    Button(
                        onClick = {
                            val targetPhone = telephone.trim()
                            if (targetPhone.isBlank()) {
                                Toast.makeText(context, "Numéro de téléphone vide", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$targetPhone")
                                    }
                                    context.startActivity(callIntent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "Aucune application de téléphone disponible", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur lors du lancement de l'appel", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Appeler")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Envoyer E-mail Button
                    Button(
                        onClick = {
                            val targetEmail = email.trim()
                            if (targetEmail.isBlank()) {
                                Toast.makeText(context, "Adresse e-mail vide", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$targetEmail")
                                    }
                                    context.startActivity(emailIntent)
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, "Aucune application de messagerie disponible", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur lors de l'envoi de l'e-mail", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("E-mail")
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Buttons: Enregistrer & Annuler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            var hasError = false
                            if (nom.trim().isEmpty()) {
                                nomError = true
                                hasError = true
                            }
                            if (prenom.trim().isEmpty()) {
                                prenomError = true
                                hasError = true
                            }
                            if (telephone.trim().isEmpty()) {
                                telephoneError = true
                                hasError = true
                            }

                            if (hasError) {
                                Toast.makeText(
                                    context,
                                    "Veuillez remplir tous les champs obligatoires (Nom, Prénom, Téléphone)",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val updatedContact = Contact(
                                    id = contactId,
                                    nom = nom.trim(),
                                    prenom = prenom.trim(),
                                    telephone = telephone.trim(),
                                    email = email.trim(),
                                    photoUri = photoUri
                                )
                                onSave(updatedContact)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enregistrer")
                    }
                }
            }
        }
    }
}

private fun launchCamera(context: Context, onUriCreated: (Uri) -> Unit) {
    try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.cacheDir, "images").apply { mkdirs() }
        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        onUriCreated(uri)
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur création fichier image: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
