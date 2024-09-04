package com.gershaveut.chat_ofg

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Chat()
    }
}

@Composable
@Preview
fun Menu() {
    Surface(modifier = Modifier.width(750.dp).height(500.dp)) {
        LazyColumn {
            items(
                listOf(
                    User("Test name 1", "Test last text 1", "00:00", 1),
                    User("Test name 2", "Test last text 2", "00:00", 10),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0),
                    User("Test name 3", "Test last text 3", "00:00", 0)
                )
            ) {
                UserRow(it)
            }
        }
    }
}

@Composable
fun UserRow(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(1.dp).clickable {
            TODO("Open chat")
        }
    ) {
        // Image box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.background(
                color = Color.LightGray,
                shape = CircleShape
            ).size(45.dp)
        ) {
            Text(user.name.toCharArray()[0].toString().uppercase())
        }
        
        // Info
        Column {
            // Row Name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(user.name, textAlign = TextAlign.Start)
                
                Text(
                    user.lastTime,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            // Row Last Text and New Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    user.lastText,
                    textAlign = TextAlign.Start,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                if (user.newMessage > 0) {
                    Text(
                        user.newMessage.toString(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.background(
                            color = Color.Cyan,
                            shape = CircleShape
                        ).size(25.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun Chat() {
    Surface(modifier = Modifier.fillMaxSize()) {
        //Chat content
        
        Column {
            LazyColumn( modifier = Modifier.weight(15f) ) {
                items(listOf(
                    ChatMessage("Ты поедешь в Пирогово, будешь меня бранить Сереже?", false),
                    ChatMessage("Я ни с кем не говорил, ни с Таней, дочерью.", true),
                    ChatMessage("Но с Таней, сестрой, говорил?", false),
                    ChatMessage("Да.", true),
                    ChatMessage("Что же она говорила?", false),
                    ChatMessage("То же, что тебе… мне тебя защищала, тебе, вероятно, за меня говорила.", true),
                    ChatMessage("Да, она ужасно строга была ко мне. Слишком строга. Я не заслуживаю.", false),
                    ChatMessage("Пожалуйста, не будем говорить, уляжется, успокоится и, бог даст, уничтожится.", true),
                    ChatMessage("Не могу я не говорить. Мне слишком тяжело жить под вечным страхом. Теперь, если он заедет, начнется опять. Он не говорил ничего, но, может быть, заедет.", false),
                    ChatMessage("Только что надеялся успокоиться, как опять ты будто приготавливаешь меня к неприятному ожиданию.", true),
                    ChatMessage("Что же мне делать? Это может быть, он сказал Тане. Я не звала. Может быть, он заедет.", false),
                    ChatMessage("Заедет он или не заедет, неважно, даже твоя поездка не важна, важно, как я говорил тебе, два года назад говорил тебе, твое отношение к твоему чувству. Если бы ты признавала свое чувство нехорошим, ты бы не стала даже и вспоминать о том, заедет ли он, и говорить о нем.", true),
                    ChatMessage("Ну, как же быть мне теперь?", false),
                    ChatMessage("Покаяться в душе в своем чувстве.", true),
                    ChatMessage("Не умею каяться и не понимаю, что это значит.", false),
                    ChatMessage("Это значит обсудить самой с собой, хорошо ли то чувство, которое ты испытываешь к этому человеку, или дурное.", true),
                    ChatMessage("Я никакого чувства не испытываю, ни хорошего, ни дурного.", false),
                    ChatMessage("Это неправда.", true),
                    ChatMessage("Чувство это так неважно, ничтожно.", false),
                    ChatMessage("Все чувства, а потому и самое ничтожное, всегда или хорошие, или дурные в наших глазах, и потому и тебе надо решить, хорошее ли это было чувство, или дурное.", true),
                    ChatMessage("Нечего решать, это чувство такое неважное, что оно не может быть дурным. Да и нет в нем ничего дурного.", false),
                    ChatMessage("Нет, исключительное чувство старой замужней женщины к постороннему мужчине — дурное чувство.", true),
                    ChatMessage("У меня нет чувства к мужчине, есть чувство к человеку.", false),
                    ChatMessage("Да ведь человек этот мужчина.", true),
                    ChatMessage("Он для меня не мужчина. Нет никакого чувства исключительного, а есть то, что после моего горя мне было утешение музыка, а к человеку нет никакого особенного чувства.", false),
                    ChatMessage("Зачем говорить неправду?", true),
                    ChatMessage("Но хорошо. Это было. Я сделала дурно, что заехала, что огорчила тебя. Но теперь это кончено, я сделаю все, чтобы не огорчать тебя.", false),
                    ChatMessage("Ты не можешь этого сделать потому, что все дело не в том, что ты сделаешь — заедешь, примешь, не примешь, дело все в твоем отношении к твоему чувству. Ты должна решить сама с собой, хорошее ли это, или дурное чувство.", true),
                    ChatMessage("Да нет никакого.", false),
                    ChatMessage("Это неправда. И вот это-то и дурно для тебя, что ты хочешь скрыть это чувство, чтобы удержать его. А до тех пор, пока ты не решишь, хорошее это чувство или дурное, и не признаешь, что оно дурное, ты будешь не в состоянии не делать мне больно. Если ты признаешь, как ты признаешь теперь, что чувство это хорошее, то никогда не будешь в силах не желать удовлетворения этого чувства, то есть видеться, а желая, ты невольно будешь делать то, чтобы видеться. Если ты будешь избегать случаев видеться, то тебе будет тоска, тяжело. Стало быть, все дело в том, чтобы решить, какое это чувство, дурное или хорошее.", true)
                )) { chatMessage ->
                    @Composable
                    fun messageContent(message: ChatMessage) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally ) {
                            if (message.owner != null) {
                                Text(message.owner!!, color = Color(41, 150, 201), fontSize = 15.sp, modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                                    .align(Alignment.Start))
                            }
                            
                            if (message.id != null) {
                                //Image(ImageBitmap.imageResource(message.id!!), null)
                            }
                            
                            Text(message.text, modifier = Modifier
                                .padding(
                                    top = if (message.owner == null) 10.dp else 0.dp,
                                    start = 10.dp,
                                    bottom = 10.dp,
                                    end = 10.dp
                                )
                                .align(Alignment.Start))
                        }
                    }
                    
                    val chatBoxModifier = Modifier.sizeIn(maxWidth = 350.dp).padding(top = 5.dp, start = 5.dp)
                    
                    if (chatMessage.isRemote) {
                        Box(
                            modifier = chatBoxModifier
                                .background(
                                    color = Color(238, 238, 238),
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            messageContent(chatMessage)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (/*LocalConfiguration.current.screenWidthDp*/ 500 > 600) Arrangement.Start else Arrangement.End) {
                            Box(
                                modifier = chatBoxModifier
                                    .background(
                                        color = Color(199, 225, 252),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                messageContent(chatMessage)
                            }
                        }
                    }
                }
            }
            
            Row {
                val message = remember{mutableStateOf("")}
                
                TextField(
                    message.value, { text ->
                        message.value = text
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    placeholder = { Text("stringResource(R.string.co_message)") }
                )
                IconButton( {
                
                },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, null)
                }
            }
        }
    }
}

class ChatMessage(var text: String, var isRemote: Boolean = true, var id: Int? = null, var owner: String? = null)

data class User(val name: String, val lastText: String, val lastTime: String, val newMessage: Int)