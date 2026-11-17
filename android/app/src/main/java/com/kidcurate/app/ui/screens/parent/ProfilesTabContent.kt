package com.kidcurate.app.ui.screens.parent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidcurate.app.data.ChildManageItem
import com.kidcurate.app.ui.components.ThematicAvatar
import com.kidcurate.app.ui.theme.*

@Composable
fun ChildProfilesTab(
    children: List<ChildManageItem>,
    onEditChild: (ChildManageItem) -> Unit,
    onDeleteChild: (ChildManageItem) -> Unit,
    onAddNewChild: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Child Profiles",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "Edit names, ages, avatars & login PINs",
                        fontSize = 12.sp,
                        color = TextSecondary,
                    )
                }

                Button(
                    onClick = onAddNewChild,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Child", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(children, key = { it.id }) { child ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BackgroundCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ThematicAvatar(name = child.name, avatar = child.avatar, size = 48.dp, iconSize = 24.dp)
                        Column {
                            Text(
                                text = child.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Age: ${child.age} yrs",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                )
                                Text("•", fontSize = 12.sp, color = TextSecondary)
                                var showPin by remember { mutableStateOf(false) }
                                val rawPin = child.pin.orEmpty()
                                val displayPin = if (rawPin.isNotBlank() && rawPin != "null" && rawPin != "••••" && !rawPin.startsWith("$2") && rawPin.all { it.isDigit() }) rawPin else "Set"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showPin = !showPin }
                                ) {
                                    Text(
                                        text = if (showPin) "PIN: $displayPin" else "PIN: ••••",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimary,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        if (showPin) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onEditChild(child) },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp, color = BrandPrimary, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { onDeleteChild(child) },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = BrandCoral,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
