package com.example.syncpassapp.feature.clases.ui
import com.example.syncpassapp.core.datasource.local.UniversidadLocalDataSource

import androidx.lifecycle.SavedStateHandle
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import com.example.syncpassapp.util.FakeUniversidadDao
import com.example.syncpassapp.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeacherHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeUniversidadDao
    private lateinit var repository: UniversidadRepository

    @Before
    fun setUp() {
        dao = FakeUniversidadDao()
        repository = UniversidadRepository(UniversidadLocalDataSource(dao))
    }

    private fun viewModel(profesorId: Long = 10L): TeacherHomeViewModel =
        TeacherHomeViewModel(
            repository,
            SavedStateHandle(mapOf("profesorId" to profesorId))
        )

    private fun TestScope.observarUiState(vm: TeacherHomeViewModel) {
        backgroundScope.launch(mainDispatcherRule.testDispatcher) {
            vm.uiState.collect { }
        }
    }

    @Test
    fun `filtro EN_CURSO solo muestra clases en curso`() = runTest {
        val id1 = repository.crearClase(ClaseEntity(nombre = "A", descripcion = "", profesorId = 10L, cuestionarioEstructuraJson = "{}", estado = "EN_CURSO"))
        repository.crearClase(ClaseEntity(nombre = "B", descripcion = "", profesorId = 10L, cuestionarioEstructuraJson = "{}", estado = "FINALIZADO"))
        val vm = viewModel()
        observarUiState(vm)

        vm.cambiarFiltro("EN_CURSO")

        assertEquals(1, vm.uiState.value.clases.size)
        assertEquals("A", vm.uiState.value.clases.single().nombre)
        assertEquals("EN_CURSO", vm.uiState.value.filtroEstado)
    }

    @Test
    fun `filtro FINALIZADO solo muestra finalizadas`() = runTest {
        repository.crearClase(ClaseEntity(nombre = "A", descripcion = "", profesorId = 10L, cuestionarioEstructuraJson = "{}", estado = "EN_CURSO"))
        repository.crearClase(ClaseEntity(nombre = "B", descripcion = "", profesorId = 10L, cuestionarioEstructuraJson = "{}", estado = "FINALIZADO"))
        val vm = viewModel()
        observarUiState(vm)

        vm.cambiarFiltro("FINALIZADO")

        assertEquals(1, vm.uiState.value.clases.size)
        assertEquals("B", vm.uiState.value.clases.single().nombre)
    }

    @Test
    fun `filtro TODOS muestra todas`() = runTest {
        repository.crearClase(ClaseEntity(nombre = "A", descripcion = "", profesorId = 10L, cuestionarioEstructuraJson = "{}", estado = "EN_CURSO"))
        repository.crearClase(ClaseEntity(nombre = "B", descripcion = "", profesorId = 10L, cuestionarioEstructuraJson = "{}", estado = "FINALIZADO"))
        val vm = viewModel()
        observarUiState(vm)

        vm.cambiarFiltro("TODOS")

        assertEquals(2, vm.uiState.value.clases.size)
    }

    @Test
    fun `crearClase delega en el repositorio con el profesorId del nav`() = runTest {
        val vm = viewModel(profesorId = 10L)

        vm.crearClase("Nueva", "Desc", "{}", "EN_CURSO")

        assertEquals(1, dao.clases.size)
        assertEquals("Nueva", dao.clases.values.single().nombre)
        assertEquals(10L, dao.clases.values.single().profesorId)
    }

    @Test
    fun `profesorId fuera del nav genera error`() {
        var lanzada = false
        try {
            viewModelSinId()
        } catch (e: IllegalStateException) {
            lanzada = true
        }
        assertTrue(lanzada)
    }

    private fun viewModelSinId(): TeacherHomeViewModel =
        TeacherHomeViewModel(repository, SavedStateHandle())
}
