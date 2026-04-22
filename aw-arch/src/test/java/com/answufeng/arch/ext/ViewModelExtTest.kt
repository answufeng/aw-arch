package com.answufeng.arch.ext

import com.answufeng.arch.base.MvvmViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelExtTest {

    class SampleVm : MvvmViewModel()

    open class VmScreen<VB, VM : MvvmViewModel>

    class DirectScreen : VmScreen<String, SampleVm>()

    open class MidScreen : VmScreen<String, SampleVm>()

    class DeepScreen : MidScreen()

    @Test
    fun `inferViewModelClass finds VM on direct generic superclass`() {
        val vm = inferViewModelClass(DirectScreen::class.java, MvvmViewModel::class.java)
        assertEquals(SampleVm::class.java, vm)
    }

    @Test
    fun `inferViewModelClass walks superclass chain`() {
        val vm = inferViewModelClass(DeepScreen::class.java, MvvmViewModel::class.java)
        assertEquals(SampleVm::class.java, vm)
    }
}
