package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Pasillo;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.PasilloRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador encargado de gestionar las operaciones relacionadas
 * con los pasillos del almacén.
 * Permite listar, crear, editar y eliminar pasillos, además de
 * gestionar las validaciones y los posibles errores.
 */
@Controller
@RequestMapping("/pasillos")
public class PasilloController {

    private static final Logger logger =
            LoggerFactory.getLogger(PasilloController.class);


    @Autowired
    private PasilloRepository pasilloRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private MessageSource messageSource;


    /**
     * Muestra la lista de todos los pasillos registrados.
     * Obtiene los datos de la base de datos y los envía a la vista.
     * Si se produce un error, muestra un mensaje de error.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista que muestra la lista de pasillos
     */
    @GetMapping
    public String listPasillos(Model model, Locale locale){
        logger.info("Solicitando la lista de pasillos...");

        List<Pasillo> listPasillos = null;

        try{
            listPasillos = pasilloRepository.findAll();
            logger.info("Se han cargado {} pasillos.", listPasillos.size());
        } catch(DataAccessException e){
            logger.error("Error al listar los pasillos: {}", e.getMessage());

            model.addAttribute("errorMessage", messageSource.getMessage
                    ("msg.pasillo-controller.list.error", null, locale));
        }

        model.addAttribute("listPasillos", listPasillos);
        return "pasillos/pasillo";
    }

    /**
     * Muestra el formulario para crear un nuevo pasillo.
     * Inicializa un objeto Pasillo y prepara el almacén para
     * poder seleccionar uno en el formulario.
     *
     * @param id identificador recibido como parámetro
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario de pasillos
     */
    @GetMapping("/new")
    public String showNewForm(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes,
                              Locale locale){
        logger.info("Mostrando formulario para nuevo pasillo.");

        Pasillo pasillo = new Pasillo();
        pasillo.setAlmacen(new Almacen());

        model.addAttribute("pasillo", pasillo);
        return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Muestra el formulario para editar un pasillo existente.
     * Busca el pasillo mediante su identificador y comprueba
     * que exista antes de mostrar el formulario.
     *
     * @param id identificador del pasillo que se quiere editar
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de pasillos si se produce algún error
     */
    @GetMapping("edit")
    public String showEditForm(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes,
                               Locale locale){
        logger.info("Mostrando formulario de edición para el pasillo con ID {}", id);

        try {
            Optional<Pasillo> pasillo = pasilloRepository.findById(id);

            if (pasillo.isEmpty()) {
                logger.warn("No se encontró el pasillo con ID {}", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.pasillo-controller.edit.notFound", null, locale));

                return "redirect:/pasillos";
            }

            model.addAttribute("pasillo", pasillo.get());

        } catch (DataAccessException e) {
            logger.error("Error al obtener el pasillo con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.pasillo-controller.edit.error", null, locale));

            return "redirect:/pasillos";
        }

        return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Recibe los datos del formulario para insertar un nuevo pasillo.
     * Comprueba que los datos sean válidos, que se haya seleccionado
     * un almacén y que el número del pasillo no esté repetido.
     * Si las comprobaciones son correctas, guarda el pasillo.
     *
     * @param pasillo objeto con los datos del pasillo recibido del formulario
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de pasillos
     */
    @PostMapping("/insert")
    public String insertPasillo(@Valid @ModelAttribute("pasillo") Pasillo pasillo, BindingResult result,
                                Model model, RedirectAttributes redirectAttributes, Locale locale){

        logger.info("Insertando nuevo pasillo número {}", pasillo.getNumero());

        try {
            checkAlmacenSelected(pasillo, result);

            if (result.hasErrors()) {
                return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
            }

            if (pasilloRepository.existsPasilloByNumero(pasillo.getNumero())) {
                logger.warn("El número de pasillo {} ya existe.", pasillo.getNumero());

                result.rejectValue("numero", "msg.pasillo-controller.insert.numeroExist");

                return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
            }

            pasilloRepository.save(pasillo);

            logger.info("Pasillo {} insertado con éxito.", pasillo.getNumero());

        } catch (DataAccessException e) {
            logger.error("Error al insertar el pasillo {}: {}",
                    pasillo.getNumero(), e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.pasillo-controller.insert.error", null, locale));
        }

        return "redirect:/pasillos";
    }

    /**
     * Recibe los datos del formulario para actualizar un pasillo existente.
     * Comprueba las validaciones, que se haya seleccionado un almacén
     * y que el número no esté asignado a otro pasillo.
     * Si todas las comprobaciones son correctas, guarda los cambios.
     *
     * @param pasillo objeto con los datos actualizados del pasillo
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de pasillos
     */
    @PostMapping("/update")
    public String updatePasillo(@Valid @ModelAttribute("pasillo") Pasillo pasillo,
                                BindingResult result, Model model,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        logger.info("Actualizando pasillo con ID {}", pasillo.getId());

        try {
            checkAlmacenSelected(pasillo, result);

            if (result.hasErrors()) {
                return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
            }

            if (pasilloRepository.existsPasilloByNumeroAndNotId(
                    pasillo.getNumero(), pasillo.getId())) {
                logger.warn("El número de pasillo {} ya existe para otro pasillo.",
                        pasillo.getNumero());

                result.rejectValue("numero", "msg.pasillo-controller.update.numeroExist");

                return loadAlmacenesAndShowForm(model, redirectAttributes, locale);
            }

            pasilloRepository.save(pasillo);

            logger.info("Pasillo con ID {} actualizado con éxito.", pasillo.getId());

        } catch (DataAccessException e) {
            logger.error("Error al actualizar el pasillo con ID {}: {}",
                    pasillo.getId(), e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.pasillo-controller.update.error", null, locale));
        }

        return "redirect:/pasillos";
    }

    /**
     * Elimina un pasillo mediante su identificador.
     * Si el pasillo tiene estanterías asociadas, la base de datos
     * impide su eliminación y se muestra el mensaje correspondiente.
     *
     * @param id identificador del pasillo que se quiere eliminar
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return redirección a la lista de pasillos
     */
    @PostMapping("/delete")
    public String deletePasillo(@RequestParam("id") Long id,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        logger.info("Eliminando pasillo con ID {}", id);

        try {
            pasilloRepository.deleteById(id);

            logger.info("Pasillo con ID {} eliminado con éxito.", id);

        } catch (DataIntegrityViolationException e) {
            // La clave ajena de estanterias impide borrar un pasillo con estanterías
            logger.warn("No se puede eliminar el pasillo con ID {}: tiene estanterías.", id);

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.pasillo-controller.delete.hasEstanterias", null, locale));

        } catch (DataAccessException e) {
            logger.error("Error al eliminar el pasillo con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.pasillo-controller.delete.error", null, locale));
        }

        return "redirect:/pasillos";
    }

    /**
     * Comprueba que se haya seleccionado un almacén para el pasillo.
     * Si no se ha seleccionado ninguno, añade un error de validación
     * al campo correspondiente.
     *
     * @param pasillo pasillo cuyos datos se quieren comprobar
     * @param result resultado donde se registra el error de validación
     */
    private void checkAlmacenSelected(Pasillo pasillo, BindingResult result) {
        if (pasillo.getAlmacen() == null || pasillo.getAlmacen().getId() == null) {
            result.rejectValue("almacen.id", "msg.pasillo.almacen.notNull");
        }
    }

    /**
     * Carga la lista de almacenes y la añade al modelo para mostrar
     * el formulario de creación o edición de un pasillo.
     * Si se produce un error al obtener los almacenes, realiza
     * una redirección a la lista de pasillos mostrando un mensaje.
     *
     * @param model modelo utilizado para enviar los almacenes a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de pasillos si se produce un error
     */
    private String loadAlmacenesAndShowForm(Model model,
                                            RedirectAttributes redirectAttributes,
                                            Locale locale) {
        try {
            model.addAttribute("listAlmacenes", almacenRepository.findAll());

            return "pasillos/pasillo-form";

        } catch (DataAccessException e) {
            logger.error("Error al cargar los almacenes: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.pasillo-controller.almacenes.error", null, locale));

            return "redirect:/pasillos";
        }
    }

}