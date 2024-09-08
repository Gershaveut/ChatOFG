package com.gershaveut.chat_ofg.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.cdtToString
import kotlinx.datetime.LocalDateTime

abstract class AbstractChat(
	var name: String,
	var sign: String,
	var createTime: LocalDateTime,
	var messages: ArrayList<Message>,
	var description: String? = null
) {
	@Composable
	fun ShowInfo() {
		Column(modifier = Modifier.padding(top = 5.dp, start = 5.dp)) {
			Row(modifier = Modifier.padding(bottom = 10.dp)) {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier.background(
						color = Color.LightGray,
						shape = CircleShape
					).size(60.dp)
				) {
					Text(name.toCharArray()[0].toString().uppercase())
				}
				
				Column {
					Text(
						name,
						textAlign = TextAlign.Start,
						modifier = Modifier.padding(start = 5.dp)
					)
					
					Text(
						sign,
						textAlign = TextAlign.Start,
						fontSize = 12.sp,
						color = Color.Gray,
						modifier = Modifier.padding(5.dp)
					)
				}
			}
			
			InfoRow(Icons.Outlined.Info, "Description", description ?: "No Description")
			
			InfoRow(Icons.Outlined.Info, "Creation Time", cdtToString(createTime))
		}
	}
}

@Composable
private fun InfoRow(icon: ImageVector, contentDescription: String, text: String) {
	Row {
		Column {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					icon,
					contentDescription = contentDescription
				)
				
				Text(contentDescription, modifier = Modifier.padding(start = 5.dp))
			}
			
			Text(text, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(5.dp))
		}
	}
}