package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleData
import com.example.model.PaymentStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminRevenueTab() {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Summary", "Transactions", "Subscriptions", "Coupons", "Refunds", "Reports")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp,
            containerColor = Color.White,
            contentColor = Color(0xFF2563EB),
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF2563EB),
                        height = 3.dp
                    )
                }
            },
            divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) Color(0xFF2563EB) else Color(0xFF64748B)
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color(0xFFF8FAFC))
        ) {
            when (selectedTabIndex) {
                0 -> RevenueSummaryTab()
                1 -> RevenueTransactionsTab()
                2 -> RevenueSubscriptionsTab()
                3 -> RevenueCouponsTab()
                4 -> RevenueRefundsTab()
                5 -> RevenueReportsTab()
            }
        }
    }
}

@Composable
fun RevenueSummaryTab() {
    val transactions = SampleData.transactionsState
    val totalRevenue = transactions.filter { it.status == PaymentStatus.SUCCESS }.sumOf { it.amount - it.discountAmount }
    val successCount = transactions.count { it.status == PaymentStatus.SUCCESS }
    val failedCount = transactions.count { it.status == PaymentStatus.FAILED }
    val refundedCount = transactions.count { it.status == PaymentStatus.REFUNDED }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Revenue Dashboard",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevenueMetricCard(
                    title = "Total Revenue",
                    value = "₹${"%.2f".format(totalRevenue)}",
                    icon = Icons.Default.CurrencyRupee,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                RevenueMetricCard(
                    title = "Success Payments",
                    value = "$successCount",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevenueMetricCard(
                    title = "Failed Payments",
                    value = "$failedCount",
                    icon = Icons.Default.Cancel,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                RevenueMetricCard(
                    title = "Refunded",
                    value = "$refundedCount",
                    icon = Icons.Default.KeyboardReturn,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Surface(
                color = Color(0xFFFEF2F2),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Security Rule Active", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        Text("Do not manually mark payments as SUCCESS. Always verify through the payment provider.", color = Color(0xFFB91C1C), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            Text(text = title, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun RevenueTransactionsTab() {
    val transactions = SampleData.transactionsState
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(transactions) { txn ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(txn.id, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        
                        val statusColor = when (txn.status) {
                            PaymentStatus.SUCCESS -> Color(0xFF10B981)
                            PaymentStatus.FAILED -> Color(0xFFEF4444)
                            PaymentStatus.PENDING -> Color(0xFFF59E0B)
                            PaymentStatus.REFUNDED -> Color(0xFF6B7280)
                            PaymentStatus.CANCELLED -> Color(0xFF9CA3AF)
                        }
                        Text(
                            text = txn.status.name,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("User: ${txn.userName} (${txn.userId})", fontSize = 13.sp, color = Color(0xFF475569))
                    Text("Plan: ${txn.planName}", fontSize = 13.sp, color = Color(0xFF475569))
                    Text("Date: ${dateFormat.format(Date(txn.timestamp))}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Method: ${txn.paymentMethod.name}", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = "₹${"%.2f".format(txn.amount - txn.discountAmount)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueSubscriptionsTab() {
    val context = LocalContext.current
    val transactions = SampleData.transactionsState.filter { it.status == PaymentStatus.SUCCESS }
    val plans = SampleData.subscriptionPlansState

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Revenue by Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        items(plans) { plan ->
            val planTxns = transactions.filter { it.planId == plan.id }
            val totalRevenue = planTxns.sumOf { it.amount - it.discountAmount }
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(plan.name, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("${planTxns.size} Purchases", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Text("₹${"%.2f".format(totalRevenue)}", fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun RevenueCouponsTab() {
    val context = LocalContext.current
    val transactions = SampleData.transactionsState.filter { it.status == PaymentStatus.SUCCESS && it.couponCode != null }
    val couponsUsed = transactions.groupBy { it.couponCode }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Coupon Usage & Impact", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        if (couponsUsed.isEmpty()) {
            item {
                Text("No coupons have been used yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        }
        couponsUsed.forEach { (code, txns) ->
            item {
                val totalDiscount = txns.sumOf { it.discountAmount }
                val netRevenue = txns.sumOf { it.amount - it.discountAmount }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(code ?: "", fontWeight = FontWeight.Black, color = Color(0xFFD97706))
                            Text("Uses: ${txns.size}", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Discount Given:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("₹${"%.2f".format(totalDiscount)}", fontSize = 13.sp, color = Color(0xFFEF4444))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Revenue:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text("₹${"%.2f".format(netRevenue)}", fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueRefundsTab() {
    val refunds = SampleData.refundsState
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Refund Requests", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        if (refunds.isEmpty()) {
            item { Text("No refund requests found.", color = Color.Gray) }
        }
        items(refunds) { refund ->
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(refund.id, fontWeight = FontWeight.Bold)
                        Text(refund.status, color = if (refund.status == "APPROVED") Color(0xFF10B981) else Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("User: ${refund.userName}", fontSize = 14.sp)
                    Text("Txn: ${refund.transactionId}", fontSize = 12.sp, color = Color.Gray)
                    Text("Reason: ${refund.reason}", fontSize = 13.sp, color = Color(0xFF475569), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dateFormat.format(Date(refund.requestedAt)), fontSize = 12.sp, color = Color.Gray)
                        Text("₹${"%.2f".format(refund.amount)}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueReportsTab() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Export Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Generate CSV or Excel reports for accounting and tax purposes.", color = Color(0xFF64748B), fontSize = 14.sp)
        
        Button(
            onClick = { Toast.makeText(context, "Exporting Daily Report...", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Daily Report")
        }
        
        Button(
            onClick = { Toast.makeText(context, "Exporting Monthly Report...", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export Monthly Report")
        }
    }
}
