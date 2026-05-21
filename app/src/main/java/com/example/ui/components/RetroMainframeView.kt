package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetroMainframeView(modifier: Modifier = Modifier) {
    var rawTextToPunch by remember { mutableStateOf("AETHER") }
    var crtScanlinesEnabled by remember { mutableStateOf(true) }
    var activeSoapCalls by remember { mutableStateOf(0) }
    var handshakeStatus by remember { mutableStateOf("MEMEX IDLE") }
    var y2kSuccessLogs by remember { mutableStateOf(listOf("MAINFRAME COGNITIVE DISK LOADED", "Y2K CLOCK DRIFT: SECURE")) }
    var soapSimulatedLogs by remember { mutableStateOf(listOf<String>()) }
    
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B07))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Neon Green CRT Header Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF33FF33), RoundedCornerShape(8.dp))
                .background(Color(0xFF0D160D))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "    ___   _________________  ___________ \n" +
                           "   /   | / ____/_  __/ / / / / ___/ ___/ \n" +
                           "  / /| |/ __/   / / / /_/ /  \\__ \\\\_  \\  \n" +
                           " / ___ / /___  / / / __  /  ___/ /__/ /  \n" +
                           "/_/  |_/_____/ /_/ /_/ /_/  /____/____/  \n" +
                           "    >> AETHER MAINFRAME COGNITIVE TERMINAL v0.99b <<",
                    color = Color(0xFF33FF33),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CORE STATUS: NOMINAL (Y2K SAFE)",
                        color = Color(0xFF33FF33),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "PORT: COM3 / SOAPv1.1",
                        color = Color(0xFF33FF33),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Left-right layout for legacy controls
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Interactive 1: Y2K Scan Tool & Analog Handshake Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A0F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22AA22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "[ SYSTEM TELEMETRY RELAY ]",
                        color = Color(0xFF33FF33),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Last Modem Log: $handshakeStatus",
                        color = Color(0xFFBBFFBB),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                y2kSuccessLogs = y2kSuccessLogs + "RUNNING ANTECEDENT ENTRANCE SCAN..."
                                handshakeStatus = "DIALING 555-4321..."
                                scope.launch {
                                    delay(1000)
                                    handshakeStatus = "CONNECT 14400bps / COGNITIVE SINK OK"
                                    y2kSuccessLogs = y2kSuccessLogs + "MODEM TELEMETRY SYNCED" + "SYSTEM MEMORY CORES: SECURED"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3D1B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("DIAL COGNITIVE MODEM", color = Color(0xFF33FF33), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                y2kSuccessLogs = listOf("INIT TESTING CORE...")
                                scope.launch {
                                    delay(600)
                                    y2kSuccessLogs = listOf(
                                        "SCANNING JODA TIME COMPLIANCE...",
                                        "COBOL MEMEX MAPPER REGISTER OK",
                                        "STATUS: Y2K IMMUNE (CLOCK ADJUSTED TO METRIC 2000-05-20)"
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3D1B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SCAN Y2K THREATS", color = Color(0xFF33FF33), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Console Output stream box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF115511))
                            .padding(8.dp)
                    ) {
                        val innerScroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(innerScroll)
                        ) {
                            y2kSuccessLogs.forEach { log ->
                                Text(
                                    text = "> $log",
                                    color = Color(0xFF22FF22),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Interactive 2: Bijections of Hollerith Punchcards Encoder
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A0F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22AA22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "[ COBOL / HOLLERITH 80-COL PUNCHCARD ENCODER ]",
                        color = Color(0xFF33FF33),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Text(
                        text = "Bijections in action: Input string updates hardware physics matrices.",
                        color = Color(0xFF88CC88),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = rawTextToPunch,
                        onValueChange = { if (it.length <= 12) rawTextToPunch = it.uppercase() },
                        label = { Text("PUNCH INPUT (MAX 12 CHAR)", color = Color(0xFF44AA44), fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF55FF55),
                            unfocusedTextColor = Color(0xFF33DD33),
                            focusedContainerColor = Color.Black,
                            unfocusedContainerColor = Color.Black,
                            focusedIndicatorColor = Color(0xFF33FF33),
                            unfocusedIndicatorColor = Color(0xFF116611)
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Bijective Punchcard visual mapping matrix
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEEDDCC)) // Classic retro paper look!
                            .border(2.dp, Color(0xFFBBAA99))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("AETHER COBOL-80 TYPE CARD", color = Color.DarkGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text("SER. 004279", color = Color.DarkGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Matrix grid representing Hollerith punched columns
                            for (row in 0..4) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rawTextToPunch.forEachIndexed { i, char ->
                                        val charVal = char.code
                                        // Bijective calculation to decide if this grid is punched "O" or empty "."
                                        val isPunched = (charVal + row + i) % 3 == 0
                                        Text(
                                            text = if (isPunched) "▇" else "·",
                                            color = if (isPunched) Color.Red else Color.Gray,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "ENCODED CONTENT: [ $rawTextToPunch ]",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Interactive 3: SOAP Web Services Sandbox
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A0F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22AA22)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[ SOAP XML TRANSCEIVER SINK ]",
                            color = Color(0xFF33FF33),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Badge(containerColor = Color(0xFFBB3333)) {
                            Text("LEGACY SOAP v1.1", color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Send structured XML envelope body parameters synchronously through HTTP port 80.",
                        color = Color(0xFF88CC88),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Button(
                        onClick = {
                            activeSoapCalls++
                            val currentCallId = activeSoapCalls
                            soapSimulatedLogs = soapSimulatedLogs + 
                                "POST /cgi-bin/aether-core.pl HTTP/1.1\n" +
                                "SOAPAction: \"urn:aether-cognitive-space/SyncThoughts\"\n" +
                                "Content-Type: text/xml; charset=utf-8\n" +
                                "<soap:Envelope>\n" +
                                "  <soap:Body>\n" +
                                "    <SyncRequest id=\"$currentCallId\">\n" +
                                "      <Text>${rawTextToPunch}</Text>\n" +
                                "    </SyncRequest>\n" +
                                "  </soap:Body>\n" +
                                "</soap:Envelope>"
                            
                            scope.launch {
                                delay(820)
                                soapSimulatedLogs = soapSimulatedLogs + 
                                    "HTTP/1.1 200 OK\n" +
                                    "Content-Type: text/xml\n" +
                                    "<soap:Envelope>\n" +
                                    "  <soap:Body>\n" +
                                    "    <SyncResponse>\n" +
                                    "      <Result>CRYSTALLIZED_CORES_SYNCHRONIZED</Result>\n" +
                                    "    </SyncResponse>\n" +
                                    "  </soap:Body>\n" +
                                    "</soap:Envelope>"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E632E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("POST SYNCHRONOUS SOAP REQUEST", color = Color(0xFFEEFFEE), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    if (soapSimulatedLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.Black)
                                .border(1.dp, Color(0xFF114411))
                                .padding(6.dp)
                        ) {
                            val innerScroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(innerScroll)
                            ) {
                                soapSimulatedLogs.forEach { log ->
                                    Text(
                                        text = log,
                                        color = if (log.contains("POST") || log.contains("SOAPAction")) Color(0xFF33FF33) else Color(0xFF00FFBB),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 11.sp
                                    )
                                    HorizontalDivider(color = Color(0xFF113311), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
