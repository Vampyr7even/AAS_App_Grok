package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
modifier = Modifier.fillMaxSize().padding(16.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
) {
    TextField(
        value = scaleName,
        onValueChange = { scaleName = it },
        label = { Text("Scale Name") }
    )
    TextField(
        value = options,
        onValueChange = { options = it },
        label = { Text("Options") }
    )
    Button(
        onClick = {
            val updatedScale = ScaleEntity(scaleId, scaleName, options)
            viewModel.updateScale(updatedScale) // Assume added to ViewModel/Repo
            navController.popBackStack()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("Update")
    }
}
}