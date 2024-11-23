package com.github.sieff.mapairtool.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.*


@Service(Service.Level.PROJECT)
@State(name = "cpsAgent.AppSettingsState", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class AppSettingsState : SimplePersistentStateComponent<AppState>(AppState()) {
    private val API_KEY_KEY = "ApiKey"

    private fun createCredentialAttributes(key: String): CredentialAttributes {
        return CredentialAttributes(
            generateServiceName("CpsAgent.ApiKeySafe", key)
        )
    }

    fun retrieveApiKey(): String? {
        val attributes = createCredentialAttributes(API_KEY_KEY)
        val passwordSafe: PasswordSafe = PasswordSafe.instance

        val credentials: Credentials? = passwordSafe[attributes]
        if (credentials != null) {
            val password: String? = credentials.getPasswordAsString()
            return password
        }

        return null
    }

    fun setApiKey(key: String) {
        val attributes = createCredentialAttributes(API_KEY_KEY)
        val credentials = Credentials("", key)
        PasswordSafe.instance.set(attributes, credentials)
    }

    override fun loadState(state: AppState) {
        val newState = AppState()
        newState.studyGroup = state.studyGroup
        if (state.apiKey.isEmpty()) {
            newState.apiKey = retrieveApiKey() ?: ""
        } else {
            newState.apiKey = state.apiKey
        }

        super.loadState(newState)
        AppSettingsPublisher.publish(newState)
    }
}