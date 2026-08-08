import type { ReactNode } from 'react'
import {
  Egg,
  LocalDrink,
  BakeryDining,
  Restaurant,
  SetMeal,
  WineBar,
  CleaningServices,
  Soap,
  Cookie,
  Inventory2,
  Storefront,
  AcUnit,
  Medication,
  MedicalServices,
  Female,
  Spa,
  Edit,
  Handyman,
  Construction,
  ShoppingBasket,
  LocalPharmacy,
  Category,
  Healing,
  Vaccines,
  Masks,
  MonitorHeart,
  Biotech,
  Colorize,
  Brush,
  Palette,
  Note,
  Draw,
  School,
  Architecture,
  Plumbing,
  ElectricalServices,
  Grass,
  Kitchen,
  Blender,
  OutdoorGrill,
  ChildCare,
  Pets,
  SportsEsports,
  MusicNote,
} from '@mui/icons-material'

export interface CategoryDef {
  name: string
  label: string
  icon: ReactNode
}

export interface ListCategoryDef {
  name: string
  label: string
  icon: ReactNode
  productCategories: string[]
}

const ic = (Comp: any, size = 20) => <Comp style={{ fontSize: size }} />

// ===== CATEGORIAS DE LISTA (nivel 1) =====

export const LIST_CATEGORIES: ListCategoryDef[] = [
  {
    name: 'Alimentos',
    label: 'Alimentos',
    icon: ic(ShoppingBasket),
    productCategories: [
      'Hortifruti', 'Laticinios', 'Padaria', 'Carnes', 'Peixaria',
      'Bebidas', 'Doces', 'Enlatados', 'Mercearia', 'Congelados',
      'Hortifruti', 'Granel', 'Sem Categoria',
    ],
  },
  {
    name: 'Farmacia',
    label: 'Farmácia',
    icon: ic(LocalPharmacy),
    productCategories: [
      'Remedios', 'Curativos', 'Absorventes', 'Cremes', 'Homeopaticos',
      'Suplementos', 'Vacinas', 'Fitoterapicos', 'Sem Categoria',
    ],
  },
  {
    name: 'Papelaria',
    label: 'Papelaria',
    icon: ic(Edit),
    productCategories: [
      'Papelaria', 'Escrita', 'Arte', 'Cadernos', 'Sem Categoria',
    ],
  },
  {
    name: 'Material de Construcao',
    label: 'Material de Construção',
    icon: ic(Construction),
    productCategories: [
      'Ferramentas', 'Material de Construcao', 'Encanamento', 'Eletrica',
      'Jardinagem', 'Sem Categoria',
    ],
  },
  {
    name: 'Geral',
    label: 'Geral',
    icon: ic(Category),
    productCategories: [
      'Hortifruti', 'Laticinios', 'Padaria', 'Carnes', 'Peixaria',
      'Bebidas', 'Limpeza', 'Higiene', 'Doces', 'Enlatados', 'Mercearia',
      'Congelados', 'Remedios', 'Curativos', 'Absorventes', 'Cremes',
      'Homeopaticos', 'Suplementos', 'Vacinas', 'Fitoterapicos',
      'Papelaria', 'Escrita', 'Arte', 'Cadernos',
      'Ferramentas', 'Material de Construcao', 'Encanamento', 'Eletrica',
      'Jardinagem', 'Cozinha', 'Criancas', 'Pets', 'Games', 'Musica',
      'Sem Categoria',
    ],
  },
]

// ===== CATEGORIAS DE PRODUTO (nivel 2) =====

export const CATEGORIES: CategoryDef[] = [
  { name: 'Hortifruti', label: 'Hortifruti', icon: ic(Egg) },
  { name: 'Laticinios', label: 'Laticínios', icon: ic(LocalDrink) },
  { name: 'Padaria', label: 'Padaria', icon: ic(BakeryDining) },
  { name: 'Carnes', label: 'Carnes', icon: ic(Restaurant) },
  { name: 'Peixaria', label: 'Peixaria', icon: ic(SetMeal) },
  { name: 'Bebidas', label: 'Bebidas', icon: ic(WineBar) },
  { name: 'Limpeza', label: 'Limpeza', icon: ic(CleaningServices) },
  { name: 'Higiene', label: 'Higiene', icon: ic(Soap) },
  { name: 'Doces', label: 'Doces', icon: ic(Cookie) },
  { name: 'Enlatados', label: 'Enlatados', icon: ic(Inventory2) },
  { name: 'Mercearia', label: 'Mercearia', icon: ic(Storefront) },
  { name: 'Congelados', label: 'Congelados', icon: ic(AcUnit) },
  { name: 'Remedios', label: 'Remédios', icon: ic(Medication) },
  { name: 'Curativos', label: 'Curativos', icon: ic(MedicalServices) },
  { name: 'Absorventes', label: 'Absorventes', icon: ic(Female) },
  { name: 'Cremes', label: 'Cremes e Pomadas', icon: ic(Spa) },
  { name: 'Homeopaticos', label: 'Homeopáticos', icon: ic(Healing) },
  { name: 'Suplementos', label: 'Suplementos', icon: ic(Biotech) },
  { name: 'Vacinas', label: 'Vacinas', icon: ic(Vaccines) },
  { name: 'Fitoterapicos', label: 'Fitoterápicos', icon: ic(Grass) },
  { name: 'Papelaria', label: 'Papelaria', icon: ic(Note) },
  { name: 'Escrita', label: 'Material de Escrita', icon: ic(Edit) },
  { name: 'Arte', label: 'Material de Arte', icon: ic(Palette) },
  { name: 'Cadernos', label: 'Cadernos e Agendas', icon: ic(School) },
  { name: 'Ferramentas', label: 'Ferramentas', icon: ic(Handyman) },
  { name: 'Material de Construcao', label: 'Material de Construção', icon: ic(Construction) },
  { name: 'Encanamento', label: 'Encanamento', icon: ic(Plumbing) },
  { name: 'Eletrica', label: 'Elétrica', icon: ic(ElectricalServices) },
  { name: 'Jardinagem', label: 'Jardinagem', icon: ic(Grass) },
  { name: 'Cozinha', label: 'Cozinha', icon: ic(Kitchen) },
  { name: 'Criancas', label: 'Crianças', icon: ic(ChildCare) },
  { name: 'Pets', label: 'Pets', icon: ic(Pets) },
  { name: 'Games', label: 'Games', icon: ic(SportsEsports) },
  { name: 'Musica', label: 'Música', icon: ic(MusicNote) },
  { name: 'Sem Categoria', label: 'Sem Categoria', icon: ic(Category) },
]

// ===== HELPERS =====

export function getCategoryIcon(name: string | null): ReactNode {
  const cat = CATEGORIES.find((c) => c.name === name)
  return cat?.icon || CATEGORIES.find((c) => c.name === 'Sem Categoria')!.icon
}

export function getCategoryLabel(name: string | null, t?: (key: string) => string): string {
  if (!name) return t ? t('cat.Sem Categoria') : 'Sem Categoria'
  return t ? t(`cat.${name}`) : name
}

export function getListCategoryIcon(name: string | null): ReactNode {
  const cat = LIST_CATEGORIES.find((c) => c.name === name)
  return cat?.icon || LIST_CATEGORIES.find((c) => c.name === 'Geral')!.icon
}

export function getListCategoryLabel(name: string | null, t?: (key: string) => string): string {
  if (!name) return t ? t('cat.Geral') : 'Geral'
  return t ? t(`cat.${name}`) : name
}

export function getProductCategoriesForList(listCategory: string | null): CategoryDef[] {
  const cat = LIST_CATEGORIES.find((c) => c.name === listCategory)
  if (!cat) return CATEGORIES
  return CATEGORIES.filter((c) => cat.productCategories.includes(c.name))
}
