package com.sju18001.petmanagement.ui.myPage.account

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentEditAccountBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.login.LoginActivity
import com.sju18001.petmanagement.ui.myPage.MyPageViewModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.regex.Pattern

class EditAccountFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private var EDIT_ACCOUNT_DIRECTORY: String = "edit_account"

    private lateinit var editAccountViewModel: EditAccountViewModel
    private var _binding: FragmentEditAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // variable for ViewModel
    private val myPageViewModel: MyPageViewModel by activityViewModels()

    // variables for storing API call(for cancel)
    private var updateAccountApiCall: Call<UpdateAccountResDto>? = null
    private var updateAccountPhotoApiCall: Call<UpdateAccountPhotoResDto>? = null
    private var updateAccountPasswordApiCall: Call<UpdateAccountPasswordResDto>? = null
    private var deleteAccountApiCall: Call<DeleteAccountResDto>? = null

    private var isViewDestroyed = false

    // pattern regex for EditTexts
    private val patternPhone: Pattern = Pattern.compile("(^02|^\\d{3})-(\\d{3}|\\d{4})-\\d{4}")
    private val patternEmail: Pattern = Patterns.EMAIL_ADDRESS
    private val patternNickname: Pattern = Pattern.compile("(^[가-힣ㄱ-ㅎa-zA-Z0-9]{2,20}$)")
    private val patternPassword: Pattern = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,20}$")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        editAccountViewModel =
            ViewModelProvider(this).get(EditAccountViewModel::class.java)

        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        (activity as AppCompatActivity)!!.supportActionBar!!.hide()

        // save account data to ViewModel(for account profile) if not already loaded
        if(!myPageViewModel.loadedFromIntent) { saveAccountDataForAccountProfile() }

        return root
    }

    override fun onStart() {
        super.onStart()

        // for view restore
        restoreState()

        // for button listeners
        binding.backButton.setOnClickListener {
            activity?.finish()
        }

        binding.confirmButton.setOnClickListener {
            if(checkIsValid()) {
                updateAccount()
            }
            else {
                Toast.makeText(context, context?.getText(R.string.account_regex_invalid), Toast.LENGTH_SHORT).show()
            }
        }

        binding.accountPhotoInputButton.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.select_photo_dialog)
            dialog.show()

            dialog.findViewById<ImageView>(R.id.close_button2).setOnClickListener { dialog.dismiss() }
            dialog.findViewById<Button>(R.id.upload_photo_button).setOnClickListener {
                dialog.dismiss()

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_PHOTO)
            }
            dialog.findViewById<Button>(R.id.use_default_image).setOnClickListener {
                dialog.dismiss()

                binding.accountPhotoInput.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_account_circle_36))
                myPageViewModel.accountPhotoByteArray = null
                myPageViewModel.accountPhotoPathValue = ""
            }
        }

        binding.passwordChangeButton.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.change_password_dialog)
            dialog.show()

            dialog.findViewById<EditText>(R.id.new_password_input).addTextChangedListener(object: TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(patternPassword.matcher(s).matches()) {
                        myPageViewModel.accountPwValid = true
                        dialog.findViewById<TextView>(R.id.pw_message2).visibility = View.GONE
                    }
                    else {
                        myPageViewModel.accountPwValid = false
                        dialog.findViewById<TextView>(R.id.pw_message2).visibility = View.VISIBLE
                    }
                    if(s.toString() == dialog.findViewById<EditText>(R.id.new_password_check_input).text.toString()) {
                        myPageViewModel.accountPwCheckValid = true
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.GONE
                    }
                    else {
                        myPageViewModel.accountPwCheckValid = false
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.VISIBLE
                    }

                    // check validation
                    dialog.findViewById<Button>(R.id.password_change_confirm_button).isEnabled =
                        myPageViewModel.accountPwValid && myPageViewModel.accountPwCheckValid
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })

            dialog.findViewById<EditText>(R.id.new_password_check_input).addTextChangedListener(object: TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(s.toString() == dialog.findViewById<EditText>(R.id.new_password_input).text.toString()) {
                         myPageViewModel.accountPwCheckValid = true
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.GONE
                    }
                    else {
                        myPageViewModel.accountPwCheckValid = false
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.VISIBLE
                    }

                    // check validation
                    dialog.findViewById<Button>(R.id.password_change_confirm_button).isEnabled =
                        myPageViewModel.accountPwValid && myPageViewModel.accountPwCheckValid
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })

            dialog.findViewById<Button>(R.id.password_change_confirm_button).setOnClickListener {
                val newPassword = dialog.findViewById<EditText>(R.id.new_password_input).text.toString()
                val password = dialog.findViewById<EditText>(R.id.password_input).text.toString()

                updateAccountPassword(newPassword, password)
                dialog.dismiss()

            }
            dialog.findViewById<Button>(R.id.password_change_cancel_button).setOnClickListener {
                dialog.dismiss()
            }

            // for hiding keyboard
            Util.setupViewsForHideKeyboard(requireActivity(), dialog.findViewById(R.id.password_change_parent_layout))
        }

        binding.logoutButton.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(context?.getString(R.string.logout_dialog))
                .setPositiveButton(
                    R.string.confirm
                ) { _, _ ->
                    logout()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .create().show()
        }

        binding.deleteAccountButton.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(context?.getString(R.string.delete_account_dialog))
                .setPositiveButton(
                    R.string.confirm
                ) { _, _ ->
                    deleteAccount()
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
                .create().show()
        }

        binding.marketingSwitch.setOnCheckedChangeListener { _, isChecked ->
            myPageViewModel.accountMarketingValue = isChecked
            if(isChecked) {
                Toast.makeText(context, context?.getText(R.string.marketing_agree), Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, context?.getText(R.string.marketing_decline), Toast.LENGTH_SHORT).show()
            }
        }

        // for EditText text change listeners
        binding.nicknameEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountNicknameValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.phoneEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountPhoneValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.emailEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myPageViewModel.accountEmailValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentEditAccountParentFragment)
    }

    override fun onResume() {
        super.onResume()

        // set views with data from ViewModel
        restoreState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        isViewDestroyed = true

        // delete copied file(if any)
        if(isRemoving || requireActivity().isFinishing) {
            Util.deleteCopiedFiles(requireContext(), EDIT_ACCOUNT_DIRECTORY)
        }

        updateAccountApiCall?.cancel()
        updateAccountPhotoApiCall?.cancel()
        updateAccountPasswordApiCall?.cancel()
        deleteAccountApiCall?.cancel()
    }

    // check regex validation
    private fun checkIsValid(): Boolean {
        return patternEmail.matcher(myPageViewModel.accountEmailValue).matches() &&
                patternNickname.matcher(myPageViewModel.accountNicknameValue).matches() &&
                patternPhone.matcher(myPageViewModel.accountPhoneValue).matches()
    }

    // update account
    private fun updateAccount() {
        // create dto
        val updateAccountReqDto = UpdateAccountReqDto(
            myPageViewModel.accountEmailValue,
            myPageViewModel.accountPhoneValue,
            myPageViewModel.accountNicknameValue,
            myPageViewModel.accountMarketingValue,
            myPageViewModel.accountUserMessageValue
        )

        updateAccountApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updateAccountReq(updateAccountReqDto)
        updateAccountApiCall!!.enqueue(object: Callback<UpdateAccountResDto> {
            override fun onResponse(
                call: Call<UpdateAccountResDto>,
                response: Response<UpdateAccountResDto>
            ) {
                if(response.isSuccessful) {
                    if(response.body()?._metadata?.status == true) {
                        updateAccountPhoto(myPageViewModel.accountPhotoPathValue)

                        // 세션 갱신
                        val prevAccount = SessionManager.fetchLoggedInAccount(requireContext())!!
                        val account = Account(
                            prevAccount.id, prevAccount.username, myPageViewModel.accountEmailValue, myPageViewModel.accountPhoneValue, null,
                            myPageViewModel.accountMarketingValue, myPageViewModel.accountNicknameValue, prevAccount.photoUrl, myPageViewModel.accountUserMessageValue
                        )
                        SessionManager.saveLoggedInAccount(requireContext(), account)
                    }
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<UpdateAccountResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    // update account photo
    private fun updateAccountPhoto(path: String) {
        // if no photo selected -> don't update photo + close
        if(path == "") {
            val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .deleteAccountPhotoReq(body)
            call.enqueue(object: Callback<DeleteAccountPhotoResDto> {
                override fun onResponse(
                    call: Call<DeleteAccountPhotoResDto>,
                    response: Response<DeleteAccountPhotoResDto>
                ) {
                    if(isViewDestroyed){
                        return
                    }

                    if(response.isSuccessful){
                        // 세션 갱신
                        val account = SessionManager.fetchLoggedInAccount(requireContext())!!
                        account.photoUrl = null
                        SessionManager.saveLoggedInAccount(requireContext(), account)

                        // close after success
                        closeAfterSuccess()
                    }else{
                        // get error message + show(Toast)
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                        // log error message
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<DeleteAccountPhotoResDto>, t: Throwable) {
                    if(isViewDestroyed){
                        return
                    }

                    // show(Toast)/log error message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    Log.d("error", t.message.toString())
                }
            })
        }
        else {
            updateAccountPhotoApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updateAccountPhotoReq(MultipartBody.Part.createFormData("file", "file.png",
                RequestBody.create(MediaType.parse("multipart/form-data"), File(path))))
            updateAccountPhotoApiCall!!.enqueue(object: Callback<UpdateAccountPhotoResDto> {
                override fun onResponse(
                    call: Call<UpdateAccountPhotoResDto>,
                    response: Response<UpdateAccountPhotoResDto>
                ) {
                    if(response.isSuccessful) {
                        // 세션 갱신
                        val account = SessionManager.fetchLoggedInAccount(requireContext())!!
                        account.photoUrl = response.body()!!.fileUrl
                        SessionManager.saveLoggedInAccount(requireContext(), account)

                        // delete copied file
                        File(path).delete()

                        // close after success
                        closeAfterSuccess()
                    }
                    else {
                        // get error message + show(Toast)
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                        // log error message
                        Log.d("error", errorMessage)
                    }
                }

                override fun onFailure(call: Call<UpdateAccountPhotoResDto>, t: Throwable) {
                    // show(Toast)/log error message
                    Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                    Log.d("error", t.message.toString())
                }

            })
        }
    }

    private fun updateAccountPassword(newPassword: String, password: String) {
        // create dto
        val updateAccountPasswordReqDto = UpdateAccountPasswordReqDto(password, newPassword)

        updateAccountPasswordApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updateAccountPasswordReq(updateAccountPasswordReqDto)
        updateAccountPasswordApiCall!!.enqueue(object: Callback<UpdateAccountPasswordResDto> {
            override fun onResponse(
                call: Call<UpdateAccountPasswordResDto>,
                response: Response<UpdateAccountPasswordResDto>
            ) {
                if(response.isSuccessful) {
                    if(response.body()?._metadata?.status == true) {
                        Toast.makeText(context, context?.getText(R.string.account_password_changed), Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, context?.getText(R.string.account_password_mismatch), Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<UpdateAccountPasswordResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun closeAfterSuccess() {
        Toast.makeText(context, context?.getText(R.string.account_update_success), Toast.LENGTH_LONG).show()
        activity?.finish()
    }

    private fun logout() {
        // remove user token in SessionManager
        SessionManager.removeUserToken(requireContext())
        SessionManager.removeLoggedInAccount(requireContext())

        // go back to login activity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(intent)
    }

    private fun deleteAccount() {
        val body = RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), "{}")

        deleteAccountApiCall = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deleteAccountReq(body)
        deleteAccountApiCall!!.enqueue(object: Callback<DeleteAccountResDto> {
            override fun onResponse(
                call: Call<DeleteAccountResDto>,
                response: Response<DeleteAccountResDto>
            ) {
                if(response.isSuccessful) {
                    if(response.body()?._metadata?.status == true) {
                        Toast.makeText(context, context?.getText(R.string.account_delete_success), Toast.LENGTH_LONG).show()
                        logout()
                    }
                }
                else {
                    // get error message + show(Toast)
                    val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                    // log error message
                    Log.d("error", errorMessage)
                }
            }

            override fun onFailure(call: Call<DeleteAccountResDto>, t: Throwable) {
                // if the view was destroyed(API call canceled) -> return
                if(_binding == null) {
                    return
                }

                // show(Toast)/log error message
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_LONG).show()
                Log.d("error", t.message.toString())
            }
        })
    }

    private fun saveAccountDataForAccountProfile() {
        myPageViewModel.loadedFromIntent = true
        myPageViewModel.accountEmailValue = requireActivity().intent.getStringExtra("email").toString()
        myPageViewModel.accountPhoneValue = requireActivity().intent.getStringExtra("phone").toString()
        myPageViewModel.accountMarketingValue = requireActivity().intent.getBooleanExtra("marketing", false)
        myPageViewModel.accountNicknameValue = requireActivity().intent.getStringExtra("nickname").toString()
        myPageViewModel.accountUserMessageValue = requireActivity().intent.getStringExtra("userMessage").toString()
        myPageViewModel.accountPhotoByteArray = requireActivity().intent.getByteArrayExtra("photoByteArray")
    }

    private fun restoreState() {
        if(myPageViewModel.accountPhotoByteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(myPageViewModel.accountPhotoByteArray, 0, myPageViewModel.accountPhotoByteArray!!.size)
            binding.accountPhotoInput.setImageBitmap(bitmap)
        }
        else if(myPageViewModel.accountPhotoPathValue != "") {
            binding.accountPhotoInput.setImageBitmap(BitmapFactory.decodeFile(myPageViewModel.accountPhotoPathValue))
        }
        else {
            // set to default image
            binding.accountPhotoInput.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_account_circle_36))
        }

        binding.nicknameEdit.setText(myPageViewModel.accountNicknameValue)
        binding.emailEdit.setText(myPageViewModel.accountEmailValue)
        binding.phoneEdit.setText(myPageViewModel.accountPhoneValue)
        binding.marketingSwitch.isChecked = myPageViewModel.accountMarketingValue!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save account photo value
        if(resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_PHOTO) {
            if (data != null) {
                // copy selected photo and get real path
                val accountPhotoPathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
                    data.data!!, EDIT_ACCOUNT_DIRECTORY)

                // file type exception -> delete copied file + show Toast message
                if (!Util.isUrlPhoto(accountPhotoPathValue)) {
                    Toast.makeText(context, context?.getText(R.string.photo_file_type_exception_message), Toast.LENGTH_LONG).show()
                    File(accountPhotoPathValue).delete()
                    return
                }

                // delete previous profile photo data
                myPageViewModel.accountPhotoByteArray = null

                // delete previously copied file(if any)
                if(myPageViewModel.accountPhotoPathValue != "") {
                    File(myPageViewModel.accountPhotoPathValue).delete()
                }

                // save path to ViewModel
                myPageViewModel.accountPhotoPathValue = accountPhotoPathValue

                // set photo to view
                binding.accountPhotoInput.setImageBitmap(BitmapFactory.decodeFile(myPageViewModel.accountPhotoPathValue))
            }
        }
    }
}