package io.horizontalsystems.bankwallet.modules.send.submodules.address

import androidx.lifecycle.ViewModel

class SendAddressPresenter(val view: SendAddressModule.IView,
                           private val interactor: SendAddressModule.IInteractor)
    : ViewModel(), SendAddressModule.IAddressModule, SendAddressModule.IInteractorDelegate,
      SendAddressModule.IViewDelegate {

    var moduleDelegate: SendAddressModule.IAddressModuleDelegate? = null

    private var enteredAddress: String? = null
    override var currentAddress: String? = null

    override fun validateAddress() {
        val address = enteredAddress ?: throw SendAddressModule.ValidationError.InvalidAddress()

        try {
            moduleDelegate?.validate(address)
            currentAddress = address
            view.setAddress(address)
            view.setAddressError(null)
        } catch (e: Exception) {
            currentAddress = null
            view.setAddress(address)
            view.setAddressError(e)
            throw e
        }
    }

    override fun validAddress() : String {
        return enteredAddress ?: throw SendAddressModule.ValidationError.InvalidAddress()
    }

    override fun didScanQrCode(address: String) {
        onAddressEnter(address)
    }

    // SendAddressModule.IViewDelegate

    override fun onViewDidLoad() {
        updatePasteButtonState()
    }

    override fun onAddressScanClicked() {
        moduleDelegate?.scanQrCode()
    }

    override fun onAddressPasteClicked() {
        interactor.addressFromClipboard?.let {
            onAddressEnter(it)
        }
    }

    override fun onAddressDeleteClicked() {
        view.setAddress(null)
        view.setAddressError(null)

        enteredAddress = null
        moduleDelegate?.onUpdateAddress()
        updatePasteButtonState()
    }

    private fun onAddressEnter(address: String) {
        val (parsedAddress, amount) = interactor.parseAddress(address)

        enteredAddress = parsedAddress

        try {
            validateAddress()
        } catch (e: Exception) {
        }

        moduleDelegate?.onUpdateAddress()

        amount?.let { parsedAmount ->
            moduleDelegate?.onUpdateAmount(parsedAmount)
        }
    }

    private fun updatePasteButtonState() {
        view.setPasteButtonState(interactor.clipboardHasPrimaryClip)
    }

}
