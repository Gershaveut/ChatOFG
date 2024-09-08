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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.chat_ofg.Colors
import com.gershaveut.chat_ofg.cdtToString
import kotlinx.datetime.LocalDateTime

abstract class AbstractChat(
	var name: String,
	var sign: String,
	var createTime: LocalDateTime,
	var messages: ArrayList<Message>,
	var description: String? = null
)