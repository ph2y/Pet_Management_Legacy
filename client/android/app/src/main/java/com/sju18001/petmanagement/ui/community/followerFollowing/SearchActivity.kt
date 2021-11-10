package com.sju18001.petmanagement.ui.community.followerFollowing

import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.sju18001.petmanagement.R
import com.sju18001.petmanagement.controller.PatternRegex
import com.sju18001.petmanagement.controller.Util
import com.sju18001.petmanagement.databinding.ActivitySearchBinding
import com.sju18001.petmanagement.restapi.RetrofitBuilder
import com.sju18001.petmanagement.restapi.ServerUtil
import com.sju18001.petmanagement.restapi.SessionManager
import com.sju18001.petmanagement.restapi.dao.Account
import com.sju18001.petmanagement.restapi.dto.*
import com.sju18001.petmanagement.ui.community.CommunityUtil
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    // variable for view binding
    private lateinit var binding: ActivitySearchBinding

    // variable for ViewModel
    private val searchViewModel: SearchViewModel by lazy{
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(SearchViewModel::class.java)
    }

    private var isViewDestroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // no title bar
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        // view binding
        binding = ActivitySearchBinding.inflate(layoutInflater)
        isViewDestroyed = false

        setContentView(binding.root)

        // searchEditText listener
        binding.searchEditText.setOnEditorActionListener{ _, _, _ ->
            checkPatternNicknameAndSearchAccount()
            true
        }
        binding.searchEditText.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchViewModel.searchEditText = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // for search button
        binding.searchButton.setOnClickListener {
            checkPatternNicknameAndSearchAccount()
        }

        // for follow unfollow button
        binding.followUnfollowButton.setOnClickListener {
            // set API/button state to loading
            searchViewModel.apiIsLoading = true
            setButtonState()

            // API call
            if(searchViewModel.accountId !in searchViewModel.followerIdList!!) {
                createFollow()
            }
            else {
                deleteFollow()
            }
        }

        // for close button
        binding.closeButton.setOnClickListener { finish() }

        // for hiding keyboard
        Util.setupViewsForHideKeyboard(this, binding.activitySearchParentLayout)
    }

    override fun onStart() {
        super.onStart()

        // initialize follower id list
        if(searchViewModel.followerIdList == null) {
            updateFollowerIdList()
        }

        restoreState()
    }

    private fun checkPatternNicknameAndSearchAccount(){
        if(!PatternRegex.checkNicknameRegex(searchViewModel.searchEditText)) {
            Toast.makeText(this, getString(R.string.nickname_regex_exception_message), Toast.LENGTH_LONG).show()
        }
        else {
            // set api state/button to loading
            searchViewModel.apiIsLoading = true
            lockViews()

            searchAccount(searchViewModel.searchEditText)
        }
    }

    private fun setAccountInfoViews(fetchAccountResDto: FetchAccountResDto) {
        // set layout's visibility to visible(if not already done)
        if(binding.accountInfoCardView.visibility != View.VISIBLE) {
            binding.accountInfoCardView.visibility = View.VISIBLE
        }

        // start pet profile
        binding.accountInfoCardView.setOnClickListener {
            CommunityUtil.fetchRepresentativePetAndStartPetProfile(this, Account(
                fetchAccountResDto.id, fetchAccountResDto.username, fetchAccountResDto.email, fetchAccountResDto.phone,
                "", fetchAccountResDto.marketing, fetchAccountResDto.nickname, fetchAccountResDto.photoUrl,
                fetchAccountResDto.userMessage, fetchAccountResDto.representativePetId), isViewDestroyed
            )
        }

        // if url is not null -> fetch photo and set it
        if(fetchAccountResDto.photoUrl != null) {
            searchViewModel.accountPhotoUrl = fetchAccountResDto.photoUrl
            fetchAccountPhoto(fetchAccountResDto.id)
        }
        // else -> reset photo related values
        else {
            searchViewModel.accountPhotoUrl = null
            searchViewModel.accountPhotoByteArray = null
            setAccountPhoto()
        }

        // save id value
        searchViewModel.accountId = fetchAccountResDto.id

        // save and set nickname value
        searchViewModel.accountNickname = fetchAccountResDto.nickname
        val nicknameText = searchViewModel.accountNickname + '님'
        binding.accountNickname.text = nicknameText

        setButtonState()
    }

    private fun setAccountPhoto() {
        if(searchViewModel.accountPhotoUrl != null) {
            binding.accountPhoto.setImageBitmap(BitmapFactory.decodeByteArray(searchViewModel.accountPhotoByteArray,
                0, searchViewModel.accountPhotoByteArray!!.size))
        }
        else {
            binding.accountPhoto.setImageDrawable(getDrawable(R.drawable.ic_baseline_account_circle_24))
        }
    }

    private fun setButtonState() {
        // exception
        if(searchViewModel.accountId == null) { return }

        // if id not in follower id list -> set button to follow
        if(searchViewModel.accountId !in searchViewModel.followerIdList!!) {
            binding.followUnfollowButton.background
                .setColorFilter(ContextCompat.getColor(this, R.color.carrot), PorterDuff.Mode.MULTIPLY)
            binding.followUnfollowButton.setTextColor(resources.getColor(R.color.white))
            binding.followUnfollowButton.text = getText(R.string.follow_button)
        }

        // if id in follower id list -> set button to unfollow
        else {
            binding.followUnfollowButton.background
                .setColorFilter(ContextCompat.getColor(this, R.color.border_line), PorterDuff.Mode.MULTIPLY)
            binding.followUnfollowButton.setTextColor(resources.getColor(R.color.black))
            binding.followUnfollowButton.text = getText(R.string.unfollow_button)
        }

        // if API is loading -> set button to loading, else -> set button to normal
        binding.followUnfollowButton.isEnabled = !searchViewModel.apiIsLoading
    }

    private fun updateFollowerIdList() {
        // reset list
        searchViewModel.followerIdList = mutableListOf()

        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!)
            .fetchFollowerReq(ServerUtil.getEmptyBody())
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, this@SearchActivity, { response ->
            response.body()!!.followerList.map {
                searchViewModel.followerIdList!!.add(it.id)
            }

            // update button state
            setButtonState()
        }, {}, {})
    }

    private fun searchAccount(nickname: String) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!)
            .fetchAccountByNicknameReq(FetchAccountReqDto(null, null, nickname))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, this@SearchActivity, { response ->
            // set api state/button to normal
            searchViewModel.apiIsLoading = false
            unlockViews()

            setAccountInfoViews(response.body()!!)
        }, { response ->
            // set api state/button to normal
            searchViewModel.apiIsLoading = false
            unlockViews()

            // get error message + handle exceptions
            when(val errorMessage = Util.getMessageFromErrorBody(response.errorBody()!!)) {
                // if no such account exists -> show Toast message
                "Account not exists" -> {
                    Util.showToastAndLog(this@SearchActivity, getString(R.string.account_does_not_exist_exception_message))
                }
                // if fetched self -> show Toast message
                "Fetched self" -> {
                    Util.showToastAndLog(this@SearchActivity, getString(R.string.fetched_self_exception_message))
                }
                // other exceptions -> show Toast message + log
                else -> {
                    Util.showToastAndLogForFailedResponse(this@SearchActivity, response.errorBody())
                }
            }
        }, {
            // set api state/button to normal
            searchViewModel.apiIsLoading = false
            unlockViews()
        })
    }

    private fun fetchAccountPhoto(id: Long) {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!)
            .fetchAccountPhotoReq(FetchAccountPhotoReqDto(id))
        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, this@SearchActivity, { response ->
            // save photo as byte array
            searchViewModel.accountPhotoByteArray = response.body()!!.byteStream().readBytes()

            setAccountPhoto()
        }, {}, {})
    }

    private fun createFollow() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!)
            .createFollowReq(CreateFollowReqDto(searchViewModel.accountId!!))

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, this@SearchActivity, {
            updateFollowerIdList()

            searchViewModel.apiIsLoading = false
        }, {
            // set api state/button to normal
            searchViewModel.apiIsLoading = false
            setButtonState()
        }, {})
    }

    private fun deleteFollow() {
        val call = RetrofitBuilder.getServerApiWithToken(SessionManager.fetchUserToken(baseContext)!!)
            .deleteFollowReq(DeleteFollowReqDto(searchViewModel.accountId!!))

        ServerUtil.enqueueApiCall(call, {isViewDestroyed}, this@SearchActivity, {
            updateFollowerIdList()

            searchViewModel.apiIsLoading = false
        }, {
            // set api state/button to normal
            searchViewModel.apiIsLoading = false
            setButtonState()
        }, {})
    }

    private fun restoreState() {
        // restore EditText
        binding.searchEditText.setText(searchViewModel.searchEditText)

        // restore account info layout
        if(searchViewModel.accountId != null) {
            binding.accountInfoCardView.visibility = View.VISIBLE

            // account photo
            if(searchViewModel.accountPhotoUrl != null) {
                binding.accountPhoto.setImageBitmap(BitmapFactory.decodeByteArray(searchViewModel.accountPhotoByteArray,
                    0, searchViewModel.accountPhotoByteArray!!.size))
            }

            // account nickname
            val nicknameText = searchViewModel.accountNickname + '님'
            binding.accountNickname.text = nicknameText

            // follow unfollow button
            setButtonState()
        }
    }

    private fun lockViews() {
        binding.searchEditText.isEnabled = false
        binding.searchButton.isEnabled = false
    }

    private fun unlockViews() {
        binding.searchEditText.isEnabled = true
        binding.searchButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()

        isViewDestroyed = true
    }
}