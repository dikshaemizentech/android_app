package com.vendtech.app.ui.activity.home

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.multidex.BuildConfig
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.gson.Gson

import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.activity.authentication.LoginActivity
import com.vendtech.app.ui.activity.meter.MeterListActivity
import com.vendtech.app.ui.activity.profile.ChangePasswordActivity
import com.vendtech.app.ui.activity.profile.EditProfileActivity
import com.vendtech.app.ui.activity.termspolicies.ContactUsActivity
import com.vendtech.app.ui.activity.termspolicies.TermsPoliciesActivity
import com.vendtech.app.ui.fragment.DashboardFragment
import com.vendtech.app.ui.fragment.WalletFragment
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.Utilities

import de.hdodenhof.circleimageview.CircleImageView
//import com.vendtech.app.BuildConfig
import com.vendtech.app.models.referral.ReferralCodeModel
import com.vendtech.app.ui.activity.profile.NotificationsListActivity
import com.vendtech.app.ui.fragment.PosListFragment
import com.vendtech.app.ui.fragment.ReportsFragment
import com.vendtech.app.utils.CustomDialog
import kotlinx.android.synthetic.main.nav_header.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeActivity : AppCompatActivity(), View.OnClickListener, DashboardFragment.NotificationCount {


    lateinit var imgNav: ImageView
    lateinit var logoHeader: ImageView
    private var drawerLayout: DrawerLayout? = null
    lateinit var navigationView: NavigationView
    lateinit var headerTitle: TextView
    internal var addMeter: TextView? = null


    //NAVIGATION MENU AND HEADER

    lateinit var dashboardLL: LinearLayout
    lateinit var posLL: LinearLayout
    lateinit var walletLL: LinearLayout
    lateinit var reportLL: LinearLayout
    lateinit var meterLL: LinearLayout
    lateinit var changepasswordLL: LinearLayout
    lateinit var notificationLL: LinearLayout
    lateinit var termsLL: LinearLayout
    lateinit var privacyLL: LinearLayout
    lateinit var shareappLL: LinearLayout
    lateinit var contactUsll: LinearLayout
    lateinit var logoutLL: LinearLayout
    lateinit var editprofileTV: TextView
    lateinit var userpic: CircleImageView
    lateinit var usernameTV: TextView
    lateinit var editUserProfile: ImageView
    lateinit var notificationCountTV: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        imgNav = findViewById(R.id.imgNav)
        drawerLayout = findViewById(R.id.activity_main)
        navigationView = findViewById(R.id.nv)
        val headerView = navigationView.getHeaderView(0)
        findNavView(headerView)
        findViews()
        Log.v("LinkCode", Utilities.GenerateInviteCode())

        imgNav.setOnClickListener { v ->
            if (drawerLayout!!.isDrawerOpen(Gravity.START))
                drawerLayout!!.closeDrawer(Gravity.START)
            else
                drawerLayout!!.openDrawer(Gravity.START)
        }


        LoadDashboardFragment()
    }


    fun findViews() {

        logoHeader = findViewById<View>(R.id.logoheader) as ImageView
        headerTitle = findViewById<View>(R.id.menuTitle) as TextView

    }

    override fun CountIs(count: String) {
        Log.v("CountIS", count)

        if (!TextUtils.isEmpty(count)) {

            var countIs = count.toInt()

            if (countIs == 0) {

                notificationCountTV.visibility = View.GONE
                notificationCountTV.text = countIs.toString()

            } else if (countIs < 100) {
                notificationCountTV.visibility = View.VISIBLE
                notificationCountTV.text = countIs.toString()

            } else {

                notificationCountTV.visibility = View.VISIBLE
                notificationCountTV.text = countIs.toString() + "+"

            }
        }
    }

    fun findNavView(navigationView: View) {


        dashboardLL = navigationView.findViewById<View>(R.id.dashboardLL) as LinearLayout
        posLL = navigationView.findViewById<View>(R.id.posLL) as LinearLayout
        walletLL = navigationView.findViewById<View>(R.id.walletLL) as LinearLayout
        reportLL = navigationView.findViewById<View>(R.id.reportLL) as LinearLayout
        meterLL = navigationView.findViewById<View>(R.id.meterLL) as LinearLayout
        changepasswordLL = navigationView.findViewById<View>(R.id.changepassLL) as LinearLayout
        notificationLL = navigationView.findViewById<View>(R.id.notificationsLL) as LinearLayout
        termsLL = navigationView.findViewById<View>(R.id.tcLL) as LinearLayout
        privacyLL = navigationView.findViewById<View>(R.id.privacyLL) as LinearLayout
        logoutLL = navigationView.findViewById<View>(R.id.logoutLL) as LinearLayout
        contactUsll = navigationView.findViewById<View>(R.id.contactusLL) as LinearLayout
        shareappLL = navigationView.findViewById<View>(R.id.shareappLL) as LinearLayout
        editprofileTV = navigationView.findViewById<View>(R.id.editprofileTV) as TextView
        usernameTV = navigationView.findViewById<View>(R.id.usernameTV) as TextView
        userpic = navigationView.findViewById<View>(R.id.userpic) as CircleImageView
        editUserProfile = navigationView.findViewById<View>(R.id.editUserProfile) as ImageView
        notificationCountTV = navigationView.findViewById<View>(R.id.notificationCountTV) as TextView


        reportLL.setOnClickListener(this)

posLL.setOnClickListener(this)
        dashboardLL.setOnClickListener(this)
        walletLL.setOnClickListener(this)
        meterLL.setOnClickListener(this)
        changepasswordLL.setOnClickListener(this)
        termsLL.setOnClickListener(this)
        privacyLL.setOnClickListener(this)
        logoutLL.setOnClickListener(this)
        contactUsll.setOnClickListener(this)
        userpic.setOnClickListener(this)
        editUserProfile.setOnClickListener(this)
        editprofileTV.setOnClickListener(this)
        shareappLL.setOnClickListener(this)
        notificationLL.setOnClickListener(this)


        SetUpProfile()

        SetUpMenuOptions()

    }


    fun SetUpProfile() {
        usernameTV.setText(SharedHelper.getString(this, Constants.USER_FNAME) + " " + SharedHelper.getString(this, Constants.USER_LNAME))
        Glide.with(this).load(SharedHelper.getString(this, Constants.USER_AVATAR)).asBitmap().error(R.drawable.dummyuser).into(userpic)
    }

    fun SetUpMenuOptions() {

        //Check whether user account status is Active or Pending
        if (SharedHelper.getString(this, Constants.USER_ACCOUNT_STATUS).equals(Constants.STATUS_ACTIVE)) {

            walletLL.alpha = 1.toFloat()
            meterLL.alpha = 1.toFloat()
            notificationLL.alpha = 1.toFloat()

            walletLL.isEnabled = true
            meterLL.isEnabled = true
            notificationLL.isEnabled = true

        } else {

            walletLL.alpha = 0.5.toFloat()
            meterLL.alpha = 0.5.toFloat()
            notificationLL.alpha = 0.5.toFloat()

            walletLL.isEnabled = false
            meterLL.isEnabled = false
            notificationLL.isEnabled = false

        }


    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.dashboardLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)

                Handler().postDelayed({ LoadDashboardFragment() }, 400)
            }
R.id.posLL ->{
    if (drawerLayout!!.isDrawerOpen(Gravity.START))
        drawerLayout!!.closeDrawer(Gravity.START)

    Handler().postDelayed({ LoadPosListFragment() }, 400)
}
            R.id.walletLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
                Handler().postDelayed({ LoadWalletFragment() }, 400)
            }

            R.id.reportLL -> {
                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
                Handler().postDelayed({ LoadReportsFragment() }, 400)
            }


            R.id.meterLL -> {
               /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, MeterListActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }


            R.id.changepassLL -> {
               /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, ChangePasswordActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }


            R.id.tcLL -> {
               /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, TermsPoliciesActivity::class.java)
                    i.putExtra("title", "OUR POLICIES")
                    i.putExtra("type", "1")
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }


            R.id.shareappLL -> {

                if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
                Handler().postDelayed({

                    GenerateRefferalCode()

                }, 400)

            }

            R.id.notificationsLL -> {

//                if (drawerLayout!!.isDrawerOpen(Gravity.START))
//                    drawerLayout!!.closeDrawer(Gravity.START)

                Handler().postDelayed({

                    val i = Intent(this@HomeActivity, NotificationsListActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

                }, 400)
            }


            R.id.contactusLL -> {
              /*  if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
*/
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, ContactUsActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }

            R.id.logoutLL -> {
              /*  if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({

                    ShowAlertForLogout()

                }, 400)
            }

            R.id.editUserProfile -> {
               /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)*/

                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, EditProfileActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }

            R.id.editprofileTV -> {
               /* if (drawerLayout!!.isDrawerOpen(Gravity.START))
                    drawerLayout!!.closeDrawer(Gravity.START)
*/
                Handler().postDelayed({
                    val i = Intent(this@HomeActivity, EditProfileActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                }, 400)
            }
        }
    }



    fun GenerateRefferalCode() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(this)
        customDialog.show()

        val call: Call<ReferralCodeModel> = Uten.FetchServerData().generate_referral_code(SharedHelper.getString(this, Constants.TOKEN))
        call.enqueue(object : Callback<ReferralCodeModel> {
            override fun onResponse(call: Call<ReferralCodeModel>, response: Response<ReferralCodeModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {

                        ShareAppLink(data.result.code)

                    } else {
                        Utilities.CheckSessionValid(data.message, this@HomeActivity, this@HomeActivity)
                    }
                }
            }

            override fun onFailure(call: Call<ReferralCodeModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", this@HomeActivity)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }
        })

    }

    fun ShareAppLink(code: String) {

        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "VendTech")
            var shareMessage = "\nLet me recommend you this application. Please install this application and use my referral code \"$code\" on Sign Up \n\n"
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {

        }


    }

    fun ShowAlertForLogout() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setMessage("Please confirm Logout!!!")
        builder.setIcon(R.drawable.appicon)
        builder.setPositiveButton("Confirm") { dialogInterface, which ->

            SharedHelper.removeUserData(this)
            SharedHelper.putBoolean(this, Constants.IS_LOGGEDIN, false)
            val i = Intent(this@HomeActivity, LoginActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            finish()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    fun ShowTitle() {
        logoHeader.visibility = View.GONE
        headerTitle.visibility = View.VISIBLE
    }

    fun ShowLogo() {
        logoHeader.visibility = View.VISIBLE
        headerTitle.visibility = View.GONE
    }

    fun ShowAddMeter() {
        logoHeader.visibility = View.GONE
        headerTitle.visibility = View.VISIBLE
    }


    fun LoadDashboardFragment() {
        ShowLogo()
        val fragment = DashboardFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_frame, fragment)
        ft.commit()
    }


    fun LoadWalletFragment() {

        ShowTitle()
        headerTitle.text = "WALLET"

        val fragment = WalletFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_frame, fragment)
        ft.commit()
    }

    fun LoadReportsFragment() {

        ShowTitle()
        headerTitle.text = "REPORTS"

        val fragment = ReportsFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_frame, fragment)
        ft.commit()
    }
    fun LoadPosListFragment() {
        ShowTitle()
        headerTitle.text = "POS"

        val fragment = PosListFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_frame, fragment)
        ft.commit()
    }
}
