package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.LoginResult
import edu.metrostate.ics342.mediatracker.data.RegisterResult
import edu.metrostate.ics342.mediatracker.data.TokenStore
import edu.metrostate.ics342.mediatracker.data.UserRepository
import edu.metrostate.ics342.mediatracker.data.model.CreateUserRequest
import edu.metrostate.ics342.mediatracker.data.model.TokenRequest
import java.io.IOException

class DefaultUserRepository(
    private val tokenStore: TokenStore,
    private val service: ApiService = RetrofitInstance.apiService,
) : UserRepository {

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String,
    ): RegisterResult {
        return try {
            val response = service.createUser(
                CreateUserRequest(
                    email        = email,
                    password     = password,
                    username     = username,
                    displayName  = displayName,
                    clientId     = ApiConstants.CLIENT_ID,
                    clientSecret = ApiConstants.CLIENT_SECRET,
                )
            )
            when (response.code()) {
                201  -> RegisterResult.Success
                409  -> RegisterResult.Conflict
                else -> RegisterResult.UnknownError
            }
        } catch (e: IOException) {
            RegisterResult.NetworkError
        } catch (e: Exception) {
            RegisterResult.UnknownError
        }
    }

    override suspend fun login(email: String, password: String): LoginResult {
        return try {
            val response = service.login(
                TokenRequest(
                    grantType    = "password",
                    email        = email,
                    password     = password,
                    clientId     = ApiConstants.CLIENT_ID,
                    clientSecret = ApiConstants.CLIENT_SECRET,
                )
            )
            val body = response.body()
            when {
                response.isSuccessful && body != null -> {
                    tokenStore.saveTokens(body.accessToken, body.refreshToken)
                    tokenStore.saveUser(body.user)
                    LoginResult.Success
                }
                response.code() == 401 -> LoginResult.InvalidCredentials
                else -> LoginResult.UnknownError
            }
        } catch (e: IOException) {
            LoginResult.NetworkError
        } catch (e: Exception) {
            LoginResult.UnknownError
        }
    }
}
