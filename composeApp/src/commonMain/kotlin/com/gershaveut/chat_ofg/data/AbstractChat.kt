package com.gershaveut.chat_ofg.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Column {
            InfoRow {
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
                    Text(name, textAlign = TextAlign.Start)

                    Text(
                        sign,
                        textAlign = TextAlign.Start,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            InfoRow {
                Column {
                    Row {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Description"
                        )

                        Text("Description")
                    }

                    Text(description ?: "No Description", fontSize = 10.sp, color = Color.Gray)
                }
            }

            InfoRow {
                Column {
                    Row {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Creation Time"
                        )

                        Text("Creation Time")
                    }

                    Text(createTime.toString(), fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(content: @Composable () -> Unit) {
    Surface {
        Row {
            content()
        }
    }
}