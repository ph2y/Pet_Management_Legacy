package com.sju18001.petmanagement.ui.setting.account

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
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
import com.sju18001.petmanagement.controller.PatternRegex
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.FragmentUpdateAccountBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.login.LoginActivity
import com.sju18001.petmanagement.ui.setting.SettingViewModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UpdateAccountFragment : Fragment() {

    // constant variables
    private val PICK_PHOTO = 0
    private var UPDATE_ACCOUNT_DIRECTORY: String = "update_account"

    private lateinit var updateAccountViewModel: UpdateAccountViewModel
    private var _binding: FragmentUpdateAccountBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // variable for ViewModel
    private val settingViewModel: SettingViewModel by activityViewModels()

    private var isViewDestroyed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        updateAccountViewModel =
            ViewModelProvider(this).get(UpdateAccountViewModel::class.java)

        _binding = FragmentUpdateAccountBinding.inflate(inflater, container, false)
        isViewDestroyed = false

        val root: View = binding.root

        (activity as AppCompatActivity)!!.supportActionBar!!.hide()

        // save account data to ViewModel(for account profile) if not already loaded
        if(!settingViewModel.loadedFromIntent) { saveAccountDataForAccountProfile() }

        return root
    }

    override fun onStart() {
        super.onStart()

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
                settingViewModel.accountPhotoByteArray = null
                if (settingViewModel.accountPhotoPathValue != "") {
                    File(settingViewModel.accountPhotoPathValue).delete()
                    settingViewModel.accountPhotoPathValue = ""
                }
                settingViewModel.isDeletePhoto = true
            }
        }

        binding.passwordChangeButton.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.change_password_dialog)
            dialog.show()

            dialog.findViewById<EditText>(R.id.new_password_input).addTextChangedListener(object: TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(PatternRegex.checkPasswordRegex(s)) {
                        settingViewModel.accountPwValid = true
                        dialog.findViewById<TextView>(R.id.pw_message2).visibility = View.GONE
                    }
                    else {
                        settingViewModel.accountPwValid = false
                        dialog.findViewById<TextView>(R.id.pw_message2).visibility = View.VISIBLE
                    }
                    if(s.toString() == dialog.findViewById<EditText>(R.id.new_password_check_input).text.toString()) {
                        settingViewModel.accountPwCheckValid = true
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.GONE
                    }
                    else {
                        settingViewModel.accountPwCheckValid = false
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.VISIBLE
                    }

                    // check validation
                    dialog.findViewById<Button>(R.id.password_change_confirm_button).isEnabled =
                        settingViewModel.accountPwValid && settingViewModel.accountPwCheckValid
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}
            })

            dialog.findViewById<EditText>(R.id.new_password_check_input).addTextChangedListener(object: TextWatcher {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(s.toString() == dialog.findViewById<EditText>(R.id.new_password_input).text.toString()) {
                         settingViewModel.accountPwCheckValid = true
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.GONE
                    }
                    else {
                        settingViewModel.accountPwCheckValid = false
                        dialog.findViewById<TextView>(R.id.pw_check_message2).visibility = View.VISIBLE
                    }

                    // check validation
                    dialog.findViewById<Button>(R.id.password_change_confirm_button).isEnabled =
                        settingViewModel.accountPwValid && settingViewModel.accountPwCheckValid
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
            settingViewModel.accountMarketingValue = isChecked
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
                settingViewModel.accountNicknameValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.phoneEdit.addTextChangedListener(PhoneNumberFormattingTextWatcher("KR"))
        binding.phoneEdit.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                settingViewModel.accountPhoneValue = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        Util.setupViewsForHideKeyboard(requireActivity(), binding.fragmentUpdateAccountParentFragment)
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
            Util.deleteCopiedFiles(requireContext(), UPDATE_ACCOUNT_DIRECTORY)
        }
    }

    // check regex validation
    private fun checkIsValid(): Boolean {
        return PatternRegex.checkNicknameRegex(settingViewModel.accountNicknameValue) &&
                PatternRegex.checkPhoneRegex(settingViewModel.accountPhoneValue)
    }

    // update account
    private fun updateAccount() {
        lockViews()

        // create dto
        val updateAccountReqDto = UpdateAccountReqDto(
            settingViewModel.accountEmailValue,
            settingViewModel.accountPhoneValue,
            settingViewModel.accountNicknameValue,
            settingViewModel.accountMarketingValue,
            settingViewModel.accountUserMessageValue,
            settingViewModel.representativePetId
        )

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updateAccountReq(updateAccountReqDto)
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            if(response.body()?._metadata?.status == true) {
                updateAccountPhoto(settingViewModel.accountPhotoPathValue)

                // 세션 갱신
                val prevAccount = SessionManager.fetchLoggedInAccount(requireContext())!!
                val account = Account(
                    prevAccount.id, prevAccount.username, settingViewModel.accountEmailValue, settingViewModel.accountPhoneValue,
                    null, settingViewModel.accountMarketingValue, settingViewModel.accountNicknameValue, prevAccount.photoUrl,
                    settingViewModel.accountUserMessageValue, settingViewModel.representativePetId
                )
                SessionManager.saveLoggedInAccount(requireContext(), account)
            }
        }, {
            unlockViews()
        }, {
            unlockViews()
        })
    }

    // update account photo
    private fun updateAccountPhoto(path: String) {
        // exception
        if (!settingViewModel.isDeletePhoto && path == "") {
            closeAfterSuccess()
            return
        }

        // delete photo
        if(settingViewModel.isDeletePhoto!!) {
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .deleteAccountPhotoReq(ServerUtil.getEmptyBody())
            call.enqueue(object: Callback<DeleteAccountPhotoResDto> {
                override fun onResponse(
                    call: Call<DeleteAccountPhotoResDto>,
                    response: Response<DeleteAccountPhotoResDto>
                ) {
                    if(isViewDestroyed) return

                    if(response.isSuccessful){
                        // 세션 갱신
                        val account = SessionManager.fetchLoggedInAccount(requireContext())!!
                        account.photoUrl = null
                        SessionManager.saveLoggedInAccount(requireContext(), account)

                        closeAfterSuccess()
                    }else{
                        val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)

                        // 사진이 애초에 없었을 경우
                        if(errorMessage == "null"){
                            closeAfterSuccess()
                        }else{
                            Util.showToastAndLogForFailedResponse(requireContext(), response.errorBody())
                        }
                    }
                }

                override fun onFailure(call: Call<DeleteAccountPhotoResDto>, t: Throwable) {
                    if(isViewDestroyed) return

                    Util.showToastAndLog(requireContext(), t.message.toString())
                }
            })
        }
        // update photo
        else {
            val updateAccountPhotoReq = MultipartBody.Part.createFormData("file", File(path).name, RequestBody.create(MediaType.parse("multipart/form-data"), File(path)))
            val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
                .updateAccountPhotoReq(updateAccountPhotoReq)
            ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
                // 세션 갱신
                val account = SessionManager.fetchLoggedInAccount(requireContext())!!
                account.photoUrl = response.body()!!.fileUrl
                SessionManager.saveLoggedInAccount(requireContext(), account)

                File(path).delete()

                closeAfterSuccess()
            }, {}, {})
        }
    }

    private fun updateAccountPassword(newPassword: String, password: String) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .updateAccountPasswordReq(UpdateAccountPasswordReqDto(password, newPassword))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            if(response.body()?._metadata?.status == true) {
                Toast.makeText(context, context?.getText(R.string.account_password_changed), Toast.LENGTH_LONG).show()
            }
        }, {}, {})
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
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(requireContext())!!)
            .deleteAccountReq(ServerUtil.getEmptyBody())
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, requireContext(), { response ->
            if(response.body()?._metadata?.status == true) {
                Toast.makeText(context, context?.getText(R.string.account_delete_success), Toast.LENGTH_LONG).show()
                logout()
            }
        }, {}, {})
    }

    private fun saveAccountDataForAccountProfile() {
        settingViewModel.loadedFromIntent = true
        settingViewModel.accountEmailValue = requireActivity().intent.getStringExtra("email").toString()
        settingViewModel.accountPhoneValue = requireActivity().intent.getStringExtra("phone").toString()
        settingViewModel.accountMarketingValue = requireActivity().intent.getBooleanExtra("marketing", false)
        settingViewModel.accountNicknameValue = requireActivity().intent.getStringExtra("nickname").toString()
        settingViewModel.accountUserMessageValue = requireActivity().intent.getStringExtra("userMessage").toString()
        settingViewModel.accountPhotoByteArray = Util.getByteArrayFromSharedPreferences(requireContext(),
            requireContext().getString(R.string.pref_name_byte_arrays),
            requireContext().getString(R.string.data_name_setting_selected_account_photo))
        settingViewModel.representativePetId = requireActivity().intent.getLongExtra("representativePetId", 0)
    }

    private fun restoreState() {
        if(settingViewModel.accountPhotoByteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(settingViewModel.accountPhotoByteArray, 0, settingViewModel.accountPhotoByteArray!!.size)
            binding.accountPhotoInput.setImageBitmap(bitmap)
        }
        else if(settingViewModel.accountPhotoPathValue != "") {
            binding.accountPhotoInput.setImageBitmap(BitmapFactory.decodeFile(settingViewModel.accountPhotoPathValue))
        }
        else {
            // set to default image
            binding.accountPhotoInput.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_baseline_account_circle_36))
        }

        binding.nicknameEdit.setText(settingViewModel.accountNicknameValue)
        binding.emailEdit.setText(settingViewModel.accountEmailValue)
        binding.phoneEdit.setText(settingViewModel.accountPhoneValue)
        binding.marketingSwitch.isChecked = settingViewModel.accountMarketingValue!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // get + save account photo value
        if(resultCode == AppCompatActivity.RESULT_OK && requestCode == PICK_PHOTO) {
            if (data != null) {
                // get file name
                val fileName = Util.getSelectedFileName(requireContext(), data.data!!)

                // copy selected photo and get real path
                val accountPhotoPathValue = ServerUtil.createCopyAndReturnRealPathLocal(requireActivity(),
                    data.data!!, UPDATE_ACCOUNT_DIRECTORY, fileName)

                // file type exception -> delete copied file + show Toast message
                if (!Util.isUrlPhoto(accountPhotoPathValue)) {
                    Toast.makeText(context, context?.getText(R.string.photo_file_type_exception_message), Toast.LENGTH_LONG).show()
                    File(accountPhotoPathValue).delete()
                    return
                }

                // delete previous profile photo data
                settingViewModel.accountPhotoByteArray = null

                // delete previously copied file(if any)
                if(settingViewModel.accountPhotoPathValue != "") {
                    File(settingViewModel.accountPhotoPathValue).delete()
                }

                // save path to ViewModel
                settingViewModel.accountPhotoPathValue = accountPhotoPathValue

                // set photo to view
                binding.accountPhotoInput.setImageBitmap(BitmapFactory.decodeFile(settingViewModel.accountPhotoPathValue))
            }
        }
    }

    private fun lockViews() {
        binding.confirmButton.visibility = View.GONE
        binding.updateAccountProgressBar.visibility = View.VISIBLE

        binding.marketingSwitch.isEnabled = false
        binding.accountPhotoInputButton.isEnabled = false
        binding.nicknameEdit.isEnabled = false
        binding.passwordChangeButton.isEnabled = false
        binding.logoutButton.isEnabled = false
        binding.deleteAccountButton.isEnabled = false
        binding.backButton.isEnabled = false
        binding.accountPhotoInput.borderColor = resources.getColor(R.color.gray)
        binding.accountPhotoInputButton.circleBackgroundColor = resources.getColor(R.color.gray)
    }

    private fun unlockViews() {
        binding.confirmButton.visibility = View.VISIBLE
        binding.updateAccountProgressBar.visibility = View.GONE

        binding.marketingSwitch.isEnabled = true
        binding.accountPhotoInputButton.isEnabled = true
        binding.nicknameEdit.isEnabled = true
        binding.passwordChangeButton.isEnabled = true
        binding.logoutButton.isEnabled = true
        binding.deleteAccountButton.isEnabled = true
        binding.backButton.isEnabled = true
        binding.accountPhotoInput.borderColor = resources.getColor(R.color.carrot)
        binding.accountPhotoInputButton.circleBackgroundColor = resources.getColor(R.color.carrot)
    }
}